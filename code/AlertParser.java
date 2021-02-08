/**
 * This class parses Snort alert data in the fast format.
 * The following line is an instance of data that can be parsed with an AlertParser:
 * 03/16-09:29:49.830000  [**] [1:384:5] ICMP PING [**] [Classification: Misc activity] [Priority: 3] {ICMP} 192.168.202.110 -> 192.168.22.245
 * Each line of data is then normalized for the k-Prototypes algorithm and made into an Entity 
**/

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.*;

/**
  *@author Patrick Bishop and Jose Quiroz based on Cooper Guzzi's alert.java and processData.java
**/

public class AlertParser
{
	private String line;

	//Data features
	private LocalDateTime atime;
	private long epochTime;
	private int sid;
	private int rev;
	private String message;
	private String classification;
	private int priority;
	private String protocol; //added since UDP and ICMP lines don't include port
	private String srcIP;
	private String destIP;
	private String srcPort;
	private String destPort;

	private HashMap<String, Integer> hmap = new HashMap<String, Integer>();
	private int counter = 1;

	public AlertParser(String l) {
		line = l;
		message = "";
		classification = "";
		protocol = "";
	}

	/*Parses through one line (one instance of alert data)*/
	public void parseLine() {
		//Start with 03/16-09:29:49.550000
		String[] list1 = line.split("\\s");
		// parse localdate time
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("MM/dd-HH:mm:ss.SSSSSS").parseDefaulting(ChronoField.YEAR, 2012).toFormatter();
		//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd-HH:mm:ss.SSSSSS");
		// set year to 2012

		atime = LocalDateTime.parse(list1[0], formatter);
		//atime = atime.withYear(2012);
		//establish time zone (use default for now)
		ZoneId zoneId = ZoneId.systemDefault();
		// convert time to Epoch Time
		epochTime = atime.atZone(zoneId).toEpochSecond();

		//next part: [**] [1:2003068:6]
		Pattern pattern = Pattern.compile("\\d+:\\d+:\\d+");
		Matcher matcher = pattern.matcher(list1[3]);
		if (matcher.find()) {
		    String str1=matcher.group();
		    String[] list2=str1.split(":");
		    sid = Integer.parseInt(list2[1]);
		    rev = Integer.parseInt(list2[2]);
		}
		//next part: ET SCAN Potential SSH Scan OUTBOUND
		int i = 4;
		while(!list1[i].equals("[**]")) {
			message += list1[i] + " ";
			i++;
		}
		//next part: [**] [Classification: Attempted Information Leak]
		i += 2;
		while(!list1[i].equals("[Priority:")) {
			if(list1[i].contains("]")) {
				String[] list3 = list1[i].split("]");
				classification += list3[0];
			}
			else
				classification += list1[i] + " ";
			i++;
		}

		//next part: [Priority: 2]
		String[] list4 = list1[++i].split("]");
		priority = Integer.parseInt(list4[0]);

		//next part: {TCP}
		i++;
		if(list1[i].contains("TCP")) {
			protocol = "TCP";
			String[] creds = list1[++i].split(":");
			srcIP = creds[0];
			srcPort = creds[1];
			i += 2;
			creds = list1[i].split(":");
			destIP = creds[0];
			destPort = creds[1];
		}
	}

	public boolean isTCP() {
		if(protocol.equals("TCP"))
			return true;
		else
			return false;
	}

	public String genCSVOutput()
	{
		String str = String.valueOf(sid)+";"+String.valueOf(rev)+";"+"\""+message+"\""+";"+classification+";"
				+ String.valueOf(priority)+ ";"+ String.valueOf(epochTime)+ ";" + srcIP + ";" + String.valueOf(srcPort)
				+";" + destIP + ";" + String.valueOf(destPort)+"\n";
		return str;
	}

	public Entity genEntityFromLine(String line) {
		try {
			double maxIP = ipToDouble("255.255.255.255");
	    String strMaxTime = "Dec 31 2012 23:59:59.999 UTC";
			SimpleDateFormat dateformat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
			Date dateMaxTime = dateformat.parse(strMaxTime);
			double epochMaxTime = (double)(dateMaxTime.getTime());
			String [] data = line.split(";");
			//process the data
			//data[] contents: sid, rev, message, classification, priority, timestamp,
			//srcIP, srcPort, destIP, destPort
			//convert IPs

			double lSrcIP = ipToDouble(data[6]);
			// String newSrcIP = String.valueOf(lSrcIP);
			double lDestIP = ipToDouble(data[8]);
			// String newDestIP = String.valueOf(lDestIP);
			double dts = Double.parseDouble(data[5]);
			String ts = String.valueOf(data[5]);
			String newSrcPort = data[7];
			String newDestPort = data[9];
			String ruleID = data[0];
			String alertType = data[3];
			Integer alertTypeInt = null;

			//normalize the data
			//IPs: divide by int val of 255.255.255.255
			String normalizedSrcIP = String.valueOf(lSrcIP/maxIP);
			String normalizedDestIP = String.valueOf(lDestIP/maxIP);
			//Ports: leave as is
			//RuleID: leave as is, will check for matches in kMeans Program
			//time: divide by epoch time of 12/31/2012 23:59:59
			String normalizedts = String.valueOf(dts/epochMaxTime);
			//AlertType: use hashmap to convert String to int value.
			if(hmap.containsKey(alertType)) {
				alertTypeInt = hmap.get(alertType);
			}
			else {
				hmap.put(alertType, counter);
				alertTypeInt = counter;
				counter++;
			}

			//normalizedSrcIP; normalizedDestIP; normalizedts(timestamp); RuleID; SrcPort; DestPort; alertTypeInt
			String normalizedFinal = normalizedSrcIP+";"+normalizedDestIP+";"+normalizedts+";"+ruleID+";"
					+srcPort+";"+destPort+";"+alertTypeInt;

			Entity en = Entity.makeEntityFromString(normalizedFinal);
			return en;
		} catch(Exception e) { return null; }
	}

	public static double ipToDouble(String ipAddress) {
    String[] ipAddressInArray = ipAddress.split("\\.");

    double result = 0;
    for (int i = 0; i < ipAddressInArray.length; i++) {

      int power = 3 - i;
      int ip = Integer.parseInt(ipAddressInArray[i]);
      result += ip * Math.pow(256, power);

    }
    return result;
  }

	/* read a snort full alert file, transfer it to a csv file */
	public static void main(String[] args) {
		try{
				FileReader fr = new FileReader("/home/m210480/Desktop/capstone/code/Fast Snort Data/file5.txt");
				BufferedReader br = new BufferedReader(fr);
				FileWriter fw = new FileWriter("output.csv");
				BufferedWriter bw = new BufferedWriter(fw);
        String line;


				while ((line = br.readLine()) != null) {
					AlertParser a = new AlertParser(line);
					a.parseLine();

					if(a.isTCP()) {
						String finishedLine = a.genCSVOutput();
						bw.write(finishedLine);
					}
				}

				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch(Throwable e){ System.out.println("Exception found!"); }
	}
}

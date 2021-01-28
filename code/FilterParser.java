import java.util.* ;
import java.io.* ;
import javax.swing.RowFilter ;

public class FilterParser {
  
  /* Enum for Filter */
  private enum Filter {
    /* Enums */
    SRC_IP(0), 
    SRC_PORT(1), 
    DEST_IP(2), 
    DEST_PORT(3), 
    CLF(4), 
    MSG(5), 
    CLUSTER(6) ;

    private String [] attributes = {"src.ip", "src.port", "dest.ip", "dest.port", "clf", "msg", "cluster" } ;
    private int val ;
    
    /* Constructor for enum */
    private Filter(int id){ this.val = id ; }
    
    /* Method to obtain the val -> index of Filter */
    public int id(){ return val ; }

    public String symbol(){ return attributes[val] ; }
  }

  /* Enum for the Logical operators */
  private enum LogicalOp {
    
    /* Enums */
    AND(0), 
    OR(1) ;

    private int val ; // id of the enum  

    /* Constructor for enum */
    private LogicalOp(int v){ this.val = v; }
    
    /* Returns id of enum */
    public int id(){ return this.val ; }
    
    /* Method creates a logical RowFilter from the supplied 
    list of filters and returns it. */
    public RowFilter<Object,Object> logicalFilter(List<RowFilter<Object,Object>> filters){
      if(this.val == 0){ return RowFilter.andFilter(filters) ; }
      return RowFilter.orFilter(filters); 
    }
    
    public String symbol(){ return ((this.val%2 == 0) ? "&&" : "||") ; }
  }
  
  
  /* Enum for Equality operators */
  private enum Equality {
    
    /* Enums */
    EQUAL(0), NOT_EQUAL(1);
    
    private int val ;

    /* Constructor for enum */
    private Equality(int id){ this.val = id ; }
    
    public String symbol(){ return ((this.val%2 == 0) ? "==" : "!=") ; }
  }
  
  
  /* Method to parse filters */
  public RowFilter<Object,Object> parseFilter(String txt){
    
    // convert string to list //
    String [] tkn = txt.split(" ") ;
    int args = tkn.length ;
    if(args%2 == 0)
      return null ;

    /* Given a single filter, just evaluate filter and
    return the RowFilter object (token should be
    a FilterToken) */
    if(args == 1){ return validateFilter(tkn[0]) ; }

    /* Given multiple filters, create 2 stacks. One will
    contain the RowFilter (which is what we need to sort tables) from
    the filter tokens. The other stack will contain all logical operators 
    in input text. */
    Stack<RowFilter<Object,Object>> filters = new Stack<RowFilter<Object,Object>>();
    Stack<LogicalOp> logicalOps = new Stack<LogicalOp>();
    for(int i=0; i < tkn.length ; i++){
      if(i%2 == 0){ // validate filter token and push it to stack
        RowFilter<Object,Object> rf = validateFilter(tkn[i]);
        if(rf == null){ return null ; }
        else{ filters.push(rf) ; }
      }
      else { // validate Logical Operator and push it to stack
        LogicalOp logicalOperator = null ;
        for(LogicalOp lo : LogicalOp.values()){
          if(tkn[i].equals(lo.symbol()))
            logicalOperator = lo ;
        }
        if(logicalOperator == null){ return null ; }
        logicalOps.push(logicalOperator) ;
      }
    }

    /* Evaluate Logical Operators */
    int count = 0 ; 
    RowFilter<Object,Object> previousFilter = null; 
    while(logicalOps.empty() == false){
      List<RowFilter<Object,Object>> filterArray = new ArrayList<RowFilter<Object,Object>>(2);
      if(count == 0){
        filterArray.add(0, filters.pop()) ;
        filterArray.add(1, filters.pop()) ;
      }
      else {
        filterArray.add(0, filters.pop()) ;
        filterArray.add(1, previousFilter) ;
      }
      previousFilter = logicalOps.pop().logicalFilter(filterArray);
      count++ ;
    }
    
    return previousFilter ; 

  }
  
  /* Method for validating a filter token */
  private RowFilter<Object, Object> validateFilter(String filterToken){
    
    // check if equality operator is valid //
    Equality equalityOp = null ; 
    for(Equality q : Equality.values()){
      if(filterToken.contains(q.symbol()))
        equalityOp = q ;
    }
    if(equalityOp == null){ return null ; }
    
    //parse token and check it has a valid filter (attribute) //
    String[] tokens = filterToken.split(equalityOp.symbol()); 
    int column = -1 ; // column to look at 
    String regex = tokens[1] ;// value/regex to look for 
    String attribute = tokens[0] ; // name of attribute (column name)
    for(Filter filter : Filter.values()){
      if(attribute.equals(filter.symbol()))
        column = filter.id() ;
    }
    if(column < 0){ return null ; }
    
    // change regex if filter has negative equality //
    if("!=".equals(equalityOp.symbol())){ regex = "^"+regex ; }

    return RowFilter.regexFilter(regex, column) ;

  }
}

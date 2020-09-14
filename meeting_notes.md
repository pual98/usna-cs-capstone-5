# Collaborative Intrusion Detection System
## 09 September 2020

+ Improve implementation on categorical attributes (this is the bottleneck in latest implementation)

+ Combine differential privacy with secure sharing to achieve better efficiency (differential privacy is more efficient but less accurate due to noise added)
    + [Homomorphic Encryption](research/When_Homomorphic_Cryptosystem_Meets_Differential_Privacy.pdf)
    + [Summary of Homomorphic Differential Privacy](https://docs.google.com/document/d/19XImfqUX-phXZn_5oWGkPs97NRoG7dks_SjsjLCiTSo/edit?usp=sharing)
    + [Differential Privacy Tutorial](research/differential_privacy_tutorial.pdf)

+ Privacy preserving solutions to other collaborative IDS tasks, e.g., rule-based, sequence-based. Currently we only do clustering of similar alerts but users can use more complex rule/sequential patterns. E.g., to identify a compromised machine used to launch attacks. (so there will be two alerts, first showing attack on the machine, second showing attack launched by that machine).  

+ Implement a GUI that allow users to detect IDS collaboratively and visualize results. It may look similar to SIEM tools. The following link give you some idea common SIEM tools [](https://www.dnsstuff.com/free-siem-tools)

+ Functional Requirements, in short:
    + Improve implementation
    + Implement differential privacy
        + Combine with secret sharing
    + Pattern based to determine attacks
        + Rule based
    + Snort alerts
        + Different data sets
        + Security data set repository
    + GUI
        + Take the low level alerts as input
        + Take the sensors and file as input
        + Tasks and result of tasks
        + Display super alerts
        + Same interfaces for everyone
            + Communicating with peers

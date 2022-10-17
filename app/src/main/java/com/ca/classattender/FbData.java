package com.ca.classattender;

public class FbData {
    public int template=0;
    public String subject="";
    public String subcode="";
    public String subteacher="";
    public String subtime="";
    public String otp="";
    public int presentcnt=0;

    public FbData(){

    }

    public FbData(int template, String subject, String subcode, String subteacher, String subtime, String otp, int presentcnt){
        this.template = template;
        this.subject = subject;
        this.subcode = subcode;
        this.subteacher = subteacher;
        this.subtime = subtime;
        this.otp = otp;
        this.presentcnt = presentcnt;
    }
}

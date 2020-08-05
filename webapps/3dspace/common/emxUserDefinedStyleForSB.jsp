<%-- emxUserDefinedStyleForSB.jsp - It gets the user defined styling 

Copyright (c) Dassault Systemes, 1993-2018 - 2008. All rights reserved. 
This program contains proprietary and trade secret information of Dassault Systems. 
Copyright notice is precautionary only and does not evidence any actual or intended 
publication of such program. 

static const char RCSID[] = $Id: emxUserDefinedStyleForSB.jsp.rca 1.1.2.1 Fri Nov  7 09:45:29 2008 ds-kvenkanna Experimental $ 
--%> 
<!-- Page display code here --> 

<%@ page import="java.util.*,java.io.*,
                com.matrixone.jdom.*,
                com.matrixone.jdom.output.*,
                com.matrixone.apps.framework.ui.UINavigatorUtil"%>

<%@include file = "emxNavigatorInclude.inc"%>
<%@include file = "emxNavigatorTopErrorInclude.inc"%>

<% 

//<!--div#mx_divBody div#mx_divTable div#mx_divTableBody table tr -->
try { 
    String strCode = MqlUtil.mqlCommand(context, "print program $1 select $2 dump","dsecUIType-Custom.css","code");
    StringBuffer strbuf2 = new StringBuffer();
    if (strCode != null && !"".equals(strCode)){
	    
        String[] strvalues = strCode.split("}");
	    String []strvalues1;
	    String []strvalues2;
	    StringBuffer strbuf = new StringBuffer();
	    StringBuffer strbuf1 = new StringBuffer();
	    
	    
	    for (int i=0; i<strvalues.length; i++) {
	        //System.out.println("strvalues[" + i +"]="+strvalues[i]);
	        strvalues1 = strvalues[i].split("\\{");
	        for (int j=0; j<strvalues1.length; j++) {
		    	
	            //System.out.println("strvalues1[" + j + "]="+strvalues1[j]);
	            
	            if (strvalues1[j].indexOf(".") != -1){
	                strbuf1.append(strvalues1[j]); 
		    	    strbuf1.append("{\n");
	            }
	            else if (strvalues1[j].indexOf("\n") != -1) {
		    	    strvalues2 = strvalues1[j].split("\n");
				    for (int z=0; z<strvalues2.length; z++){
				        String test = null;
				        if (!"".equals(strvalues2[z]) && strvalues2[z] != null ){
				            if (strvalues2[z].indexOf(";") != -1){
					            test = com.matrixone.apps.domain.util.FrameworkUtil.findAndReplace(strvalues2[z], ";", "!important;\n"); 
					        }
					        else {
					            test = strvalues2[z]; 
					        }
					        strbuf.append(test);
					        strbuf.append("!important;\n");
					       }
				    }
		    	}
	            else if (strvalues1[j].indexOf(";") != -1){
	                String test = com.matrixone.apps.domain.util.FrameworkUtil.findAndReplace(strvalues1[j], ";", "!important;\n"); 
	                strbuf.append(strvalues1[j]);
			        strbuf.append("!important;\n");
	            }
		    	
		    	strbuf1.append(strbuf.toString());
		    	strbuf = new StringBuffer();
	        }
	    	
	        strbuf2.append (strbuf1.toString());
	        strbuf2.append ("}\n"); 
	        strbuf1 = new StringBuffer();
        }
	    
    }
    String str = strbuf2.toString();
    //System.out.println("str = "+str);
    out.clear();
    response.setHeader("Content-Type", "text/css; charset=UTF-8");
	response.setContentType("text/css; charset=UTF-8");
	long now = System.currentTimeMillis();
	//response.setDateHeader("Expires", now + 7 * 24 * 60 * 60 * 1000);
	response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
	response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    out.print(str);
    
} 
catch (Exception ex) {
    //System.out.println("ex = "+ex);
	if (ex.toString() != null && ex.toString().length() > 0) { 
		emxNavErrorObject.addMessage(ex.toString()); 
%>
        <%@include file = "emxNavigatorBottomErrorInclude.inc"%>
<%
	} 
} 
%> 

package org.jtc.common.ejb.api.intf;


public interface Error {

  public long getErrorCode();

  public void setErrorCode(long error_Code);

  public String getErrorCodeMessage();

  public void setErrorCodeMessage(String error_Code_Message);

  public String getErrorCodeName();

  public void setErrorCodeName(String error_Code_Name);

  public java.util.Date getErrorDate();

  public void setErrorDate(java.util.Date error_Date);

  public long getErrorGroupId();

  public void setErrorGroupId(long error_Group_Id);

}

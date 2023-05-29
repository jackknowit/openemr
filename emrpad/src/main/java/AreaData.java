import java.util.Base64;

public class AreaData {
    Log logger=new Log("ElementData");
    public com.sun.star.text.XTextField Field;
    public com.sun.star.beans.XPropertySet Prop;
    //param1 format:
    // GroupID|ID|ActionType|Editable|ValCode|Data
    //sample:
    // 1024|TMP_ID_1024|数值|W|S01|0$0
    // 1024|TMP_ID_1024|单选|R|S02|单选项1$单选项2$单选项3

    public String GroupID;
    public String ID;
    public String ActionType;
    public String Editable;
    public String HighValue;
    public String LowValue;
    public String[] EnumValue;
    public String Value;
    public String UrlWithoutHost;
    public String WebDialogWidth;
    public String WebDialogHeight;

    public String DateMask;
    public String TimeMask;

    public String ScriptParams;
    public String Script ;

    public String ValCode;

    public void clear()
    {
        GroupID = "";
        ID = "";
        ActionType = "";
        Editable = "True";
        HighValue = "0";
        LowValue = "0";
        EnumValue = new String[] { };
        UrlWithoutHost = "";
        WebDialogWidth = "";
        WebDialogHeight = "";

        DateMask = "";
        TimeMask = "";

        Value = "";

        ScriptParams = "";
        Script = "";

        ValCode = "";

        Prop = null;
    }

    public String saveBase64()
    {
        try
        {
            String sret = "";
            sret = GroupID + "|" + ID + "|" + ActionType + "|" + Editable + "|" + encodeBase64(ValCode) + "|";

            //DisplayID 唯一编码
            //ID 结构化编码
            //ActionType 结构化类型
            //Editable 可编辑
            //ValCode 值编码

            if (ActionType.equals("数值"))
            {
                sret = sret + HighValue + "$" + LowValue;

            }
            else if (ActionType.equals("单选") ||   ActionType.equals("多选"))
            {
                int iEnumSize = EnumValue.length;
                if (iEnumSize > 0)
                {
                    for (int i = 0; i < iEnumSize; i++)
                    {
                        String s = EnumValue[i];
                        sret = sret + encodeBase64(s);
                        if (i != (iEnumSize - 1))
                            sret = sret + "$";
                    }
                }
            }
            else if (ActionType.equals("自由文本"))
            {


            }
            else if (ActionType.equals("时间"))
            {
                sret = sret + encodeBase64(DateMask) + "$" + encodeBase64(TimeMask);

            }
            else if (ActionType.equals("WEB对话框"))
            {
                sret = sret + encodeBase64(UrlWithoutHost) + "$" + WebDialogWidth + "$" + WebDialogHeight;

            }
            else if (ActionType.equals("脚本"))
            {
                sret = sret + encodeBase64(ScriptParams) + "$" + encodeBase64(Script);
            }

            String stmp = sret; //把回车换行符号替换成英文字母标识
            stmp=stmp.replace("\n", "SYM_SPEC_N");
            stmp=stmp.replace("\r", "SYM_SPEC_R");

            return stmp;


        }
        catch (Exception ex)
        {
            logger.error(ex);
            return null;
        }

    }
    public Boolean parseBase64(String sElementData)
    {
        try
        {
            clear();

            if (null==sElementData || sElementData.length() <= 0)
                return false;

            String stmp = sElementData; //把英文标识替换成回车换行符
            stmp=stmp.replace("SYM_SPEC_N", "\n");
            stmp=stmp.replace("SYM_SPEC_R", "\r");

            String[] sar1 = stmp.split("\\|");
            if (sar1.length < 4)
            {
                return false;
            }

            GroupID = sar1[0];
            ID = sar1[1];
            ActionType = sar1[2];
            Editable = sar1[3];
            ValCode = "";

            String sData = "";
            int m_iSegment = sar1.length;

            if (6 == m_iSegment)
            {
                ValCode =decodeBase64( sar1[4]);
                sData = sar1[5];
            }

            if (sData.length() > 0)
            {
                String[] arData = sData.split("\\$");

                if ( ActionType.equals("数值") && arData.length == 2)
                {
                    HighValue = arData[0];
                    LowValue = arData[1];

                }
                else if (ActionType.equals("单选") ||  ActionType.equals("多选"))
                {
                    EnumValue = arData;

                    for (int i = 0; i < EnumValue.length; i++)
                        EnumValue[i] = decodeBase64(EnumValue[i]);

                }
                else if (ActionType.equals("自由文本"))
                {


                }
                else if (ActionType.equals("时间"))
                {
                    if (arData.length == 2)
                    {
                        DateMask = decodeBase64(arData[0]);
                        TimeMask = decodeBase64(arData[1]);
                    }
                    else
                    {
                        DateMask = decodeBase64("%Y.%m.%d");
                        TimeMask = "";
                    }

                }
                else if (ActionType.equals("WEB对话框") && arData.length == 3)
                {
                    UrlWithoutHost = decodeBase64(arData[0]);
                    WebDialogWidth = arData[1];
                    WebDialogHeight = arData[2];

                }
                else if (ActionType.equals("脚本") && arData.length == 2)
                {
                    ScriptParams = decodeBase64(arData[0]);
                    Script = decodeBase64(arData[1]);
                }

            }


            return true;
        }
        catch (Exception ex)
        {
            logger.error(ex);
            return false;
        }

    }
    private String encodeBase64(String str)
    {
        try
        {
            return Base64.getEncoder().encodeToString(str.getBytes());
        }
        catch(Exception ex)
        {
            logger.error(ex);
            return null;
        }

    }
    private String decodeBase64(String str)
    {
        try
        {
            byte[] decodedBytes = Base64.getDecoder().decode(str);
            String decodedString = new String(decodedBytes);

            return decodedString;
        }
        catch (Exception ex)
        {
            logger.error(ex);
            return null;
        }
    }

}

package org.jamon;

public class StringUtils
{
    private StringUtils() { }

    private static final String PS = System.getProperty("path.separator");
    private static final String FS = System.getProperty("file.separator");

    public static String pathToClassName(String p_path)
    {
        StringBuffer sb = new StringBuffer(p_path);
        final int len = FS.length();
        while (sb.length() > 0 && FS.equals(sb.substring(0,len)))
        {
            sb.delete(0,len);
        }
        int j = len;
        for (int i = 0; i < sb.length() - len; ++i)
        {
            if (sb.substring(i,j).equals(FS))
            {
                sb.replace(i,j,".");
            }
            j++;
        }
        return sb.toString();
    }

    public static String classNameToPath(String p_className)
    {
        StringBuffer sb = new StringBuffer(FS);
        sb.append(p_className);
        for (int i = FS.length(); i < sb.length(); ++i)
        {
            if (sb.charAt(i) == '.')
            {
                sb.replace(i,i+1,FS);
            }
        }
        return sb.toString();
    }

    public static String capitalize(String p_string)
    {
        if (p_string == null)
        {
            return null;
        }
        else
        {
            char [] chars = p_string.toCharArray();
            if (chars.length == 0)
            {
                return p_string;
            }
            else
            {
                chars[0] = Character.toUpperCase(chars[0]);
                return new String(chars);
            }
        }
    }


}

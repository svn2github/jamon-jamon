package org.modusponens.jtt;

public class PathUtils
{
    private PathUtils() { }

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

}

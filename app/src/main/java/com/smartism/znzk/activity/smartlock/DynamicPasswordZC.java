package com.smartism.znzk.activity.smartlock;

import java.util.Arrays;
final class MD5
{
    private static final int MD5_S11 = 7;
    private static final int MD5_S12 = 12;
    private static final int MD5_S13 = 17;
    private static final int MD5_S14 = 22;
    private static final int MD5_S21 = 5;
    private static final int MD5_S22 = 9;
    private static final int MD5_S23 = 14;
    private static final int MD5_S24 = 20;
    private static final int MD5_S31 = 4;
    private static final int MD5_S32 = 11;
    private static final int MD5_S33 = 16;
    private static final int MD5_S34 = 23;
    private static final int MD5_S41 = 6;
    private static final int MD5_S42 = 10;
    private static final int MD5_S43 = 15;
    private static final int MD5_S44 = 21;

    private static final int MD5_F(int x, int y, int z)
    {
        return (((x) & (y)) | ((~(x)) & (z)));
    }
    private static final int MD5_G(int x, int y, int z)
    {
        return (((x) & (z)) | ((y) & (~(z))));
    }
    private static final int MD5_H(int x, int y, int z)
    {
        return (((x) ^ (y) ^ (z)));
    }
    private static final int MD5_I(int x, int y, int z)
    {
        return ((y) ^ ((x) | (~(z))));
    }
    private static final int MD5_ROL(int x, int n)
    {
        return ((x << n) | ((x >>> (32 - n))));
    }

    private static final int MD5_FF(int a, int b, int c, int d, int x, int s, int ac)
    {
        (a) += (MD5_F(b, c, d) + x + ac);
        (a) = MD5_ROL(a, s);
        (a) += (b);

        return a;
    }

    private static final int MD5_GG(int a, int b, int c, int d, int x, int s, int ac)
    {
        (a) += (MD5_G(b, c, d) + x + ac);
        (a) = MD5_ROL(a, s);
        (a) += (b);
        return a;
    }

    private static final int MD5_HH(int a, int b, int c, int d, int x, int s, int ac)
    {
        (a) += (MD5_H(b, c, d) + x + ac);
        (a) = MD5_ROL(a, s);
        (a) += (b);
        return a;
    }

    private static final int MD5_II(int a, int b, int c, int d, int x, int s, int ac)
    {
        (a) += (MD5_I(b, c, d) + x + ac);
        (a) = MD5_ROL(a, s);
        (a) += (b);
        return a;
    }

    private static final void hash_code_init(int[] hash_code)
    {
        hash_code[0] = 0x67452301;
        hash_code[1] = 0xefcdab89;
        hash_code[2] = 0x98badcfe;
        hash_code[3] = 0x10325476;
    }
    private static final void msg_hash(int[] hash_code, int[] message_group)
    {
        int a, b, c, d;

        a = hash_code[0];
        b = hash_code[1];
        c = hash_code[2];
        d = hash_code[3];

        a = MD5_FF(a, b, c, d, message_group[0], MD5_S11, 0xd76aa478);
        d = MD5_FF(d, a, b, c, message_group[1], MD5_S12, 0xe8c7b756);
        c = MD5_FF(c, d, a, b, message_group[2], MD5_S13, 0x242070db);
        b = MD5_FF(b, c, d, a, message_group[3], MD5_S14, 0xc1bdceee);
        a = MD5_FF(a, b, c, d, message_group[4], MD5_S11, 0xf57c0faf);
        d = MD5_FF(d, a, b, c, message_group[5], MD5_S12, 0x4787c62a);
        /*
        c = MD5_FF(c, d, a, b, message_group[6], MD5_S13, 0xa8304613);
        b = MD5_FF(b, c, d, a, message_group[7], MD5_S14, 0xfd469501);
        a = MD5_FF(a, b, c, d, message_group[8], MD5_S11, 0x698098d8);
        d = MD5_FF(d, a, b, c, message_group[9], MD5_S12, 0x8b44f7af);
        c = MD5_FF(c, d, a, b, message_group[10], MD5_S13, 0xffff5bb1);
        b = MD5_FF(b, c, d, a, message_group[11], MD5_S14, 0x895cd7be);
        a = MD5_FF(a, b, c, d, message_group[12], MD5_S11, 0x6b901122);
        d = MD5_FF(d, a, b, c, message_group[13], MD5_S12, 0xfd987193);
        c = MD5_FF(c, d, a, b, message_group[14], MD5_S13, 0xa679438e);
        b = MD5_FF(b, c, d, a, message_group[15], MD5_S14, 0x49b40821);
		*/

        a = MD5_GG(a, b, c, d, message_group[1], MD5_S21, 0xf61e2562);
        //d = MD5_GG(d, a, b, c, message_group[6], MD5_S22, 0xc040b340);
        //c = MD5_GG(c, d, a, b, message_group[11], MD5_S23, 0x265e5a51);
        b = MD5_GG(b, c, d, a, message_group[0], MD5_S24, 0xe9b6c7aa);
        a = MD5_GG(a, b, c, d, message_group[5], MD5_S21, 0xd62f105d);
        //d = MD5_GG(d, a, b, c, message_group[10], MD5_S22, 0x2441453);
        //c = MD5_GG(c, d, a, b, message_group[15], MD5_S23, 0xd8a1e681);
        b = MD5_GG(b, c, d, a, message_group[4], MD5_S24, 0xe7d3fbc8);
        //a = MD5_GG(a, b, c, d, message_group[9], MD5_S21, 0x21e1cde6);
        //d = MD5_GG(d, a, b, c, message_group[14], MD5_S22, 0xc33707d6);
        c = MD5_GG(c, d, a, b, message_group[3], MD5_S23, 0xf4d50d87);
        //b = MD5_GG(b, c, d, a, message_group[8], MD5_S24, 0x455a14ed);
        //a = MD5_GG(a, b, c, d, message_group[13], MD5_S21, 0xa9e3e905);
        d = MD5_GG(d, a, b, c, message_group[2], MD5_S22, 0xfcefa3f8);
        //c = MD5_GG(c, d, a, b, message_group[7], MD5_S23, 0x676f02d9);
        //b = MD5_GG(b, c, d, a, message_group[12], MD5_S24, 0x8d2a4c8a);

        a = MD5_HH(a, b, c, d, message_group[5], MD5_S31, 0xfffa3942);
        //d = MD5_HH(d, a, b, c, message_group[8], MD5_S32, 0x8771f681);
        //c = MD5_HH(c, d, a, b, message_group[11], MD5_S33, 0x6d9d6122);
        //b = MD5_HH(b, c, d, a, message_group[14], MD5_S34, 0xfde5380c);
        a = MD5_HH(a, b, c, d, message_group[1], MD5_S31, 0xa4beea44);
        d = MD5_HH(d, a, b, c, message_group[4], MD5_S32, 0x4bdecfa9);
        //c = MD5_HH(c, d, a, b, message_group[7], MD5_S33, 0xf6bb4b60);
        //b = MD5_HH(b, c, d, a, message_group[10], MD5_S34, 0xbebfbc70);
        //a = MD5_HH(a, b, c, d, message_group[13], MD5_S31, 0x289b7ec6);
        d = MD5_HH(d, a, b, c, message_group[0], MD5_S32, 0xeaa127fa);
        c = MD5_HH(c, d, a, b, message_group[3], MD5_S33, 0xd4ef3085);
        //b = MD5_HH(b, c, d, a, message_group[6], MD5_S34, 0x4881d05);
        //a = MD5_HH(a, b, c, d, message_group[9], MD5_S31, 0xd9d4d039);
        //d = MD5_HH(d, a, b, c, message_group[12], MD5_S32, 0xe6db99e5);
        //c = MD5_HH(c, d, a, b, message_group[15], MD5_S33, 0x1fa27cf8);
        b = MD5_HH(b, c, d, a, message_group[2], MD5_S34, 0xc4ac5665);

        a = MD5_II(a, b, c, d, message_group[0], MD5_S41, 0xf4292244);
        //d = MD5_II(d, a, b, c, message_group[7], MD5_S42, 0x432aff97);
        //c = MD5_II(c, d, a, b, message_group[14], MD5_S43, 0xab9423a7);
        b = MD5_II(b, c, d, a, message_group[5], MD5_S44, 0xfc93a039);
        //a = MD5_II(a, b, c, d, message_group[12], MD5_S41, 0x655b59c3);
        d = MD5_II(d, a, b, c, message_group[3], MD5_S42, 0x8f0ccc92);
        //c = MD5_II(c, d, a, b, message_group[10], MD5_S43, 0xffeff47d);
        b = MD5_II(b, c, d, a, message_group[1], MD5_S44, 0x85845dd1);
        //a = MD5_II(a, b, c, d, message_group[8], MD5_S41, 0x6fa87e4f);
        //d = MD5_II(d, a, b, c, message_group[15], MD5_S42, 0xfe2ce6e0);
        //c = MD5_II(c, d, a, b, message_group[6], MD5_S43, 0xa3014314);
        //b = MD5_II(b, c, d, a, message_group[13], MD5_S44, 0x4e0811a1);
        a = MD5_II(a, b, c, d, message_group[4], MD5_S41, 0xf7537e82);
        //d = MD5_II(d, a, b, c, message_group[11], MD5_S42, 0xbd3af235);
        c = MD5_II(c, d, a, b, message_group[2], MD5_S43, 0x2ad7d2bb);
        //b = MD5_II(b, c, d, a, message_group[9], MD5_S44, 0xeb86d391);

        hash_code[0] += a;
        hash_code[1] += b;
        hash_code[2] += c;
        hash_code[3] += d;

        hash_code[0] ^= hash_code[1];
        hash_code[0] ^= hash_code[2];
        hash_code[0] ^= hash_code[3];
    }

    private static final void byteToInt32_Little_endian(byte[] array_byte, int[] array_int)
    {
        int i, j;
        int cnt;

        cnt = array_byte.length;
        for(i = 0, j = 0; i < cnt; i += 4, j++)
        {
            array_int[j] = ((int)array_byte[i]) & 0x000000ff;
            array_int[j] |= (array_byte[i + 1] << 8) & 0x0000ff00;
            array_int[j] |= (array_byte[i + 2] << 16) & 0x00ff0000;
            array_int[j] |= (array_byte[i + 3] << 24) & 0xff000000;
        }
    }

    private static final int[] getMd5(byte[] src)
    {
        int[] hash_code = new int[4];
        Arrays.fill(hash_code, 0);

        if(src == null)
            return hash_code;

        int[] message_group_int = new int[16];
        byte[] message_group_byte = new byte[64];

        int total_len;
        int total_len_bit;
        int part_len;
        int cur_len;
        int i;

        total_len = src.length;
        total_len_bit = total_len << 3;
        cur_len = 0;

        hash_code_init(hash_code);

        do
        {
            part_len = total_len - cur_len;
            if(part_len <= 56)
            {
                //复制数据
                System.arraycopy(src, cur_len, message_group_byte, 0, part_len);

                //填充1个1和N个0
                if(part_len < 56)
                    message_group_byte[part_len] = (byte) 0x80;
                for(i = part_len + 1; i < 56; i++)
                {
                    message_group_byte[i] = 0x00;
                }
                //填充数据长度
                //受平台大小端模式影响，不要直接赋值，可移植性差
                //message_group_int[14] = total_len_bit << 3;
                //message_group_int[15] = 0;
                message_group_byte[56] = (byte)(total_len_bit & 0xff);
                message_group_byte[57] = (byte)((total_len_bit >>> 8) & 0xff);
                message_group_byte[58] = (byte)((total_len_bit >>> 16) & 0xff);
                message_group_byte[59] = (byte)((total_len_bit >>> 24) & 0xff);
                message_group_byte[60] = 0;
                message_group_byte[61] = 0;
                message_group_byte[62] = 0;
                message_group_byte[63] = 0;
                //message_group_int[14] = (total_len << 3);
                //message_group_int[15] = 0;
                //转换为小端模式
                byteToInt32_Little_endian(message_group_byte, message_group_int);
                //数据分组转换
                msg_hash(hash_code, message_group_int);
                break;
            }
            else if(part_len <= 63)
            {
                //分成两次分组转换

                //复制数据
                System.arraycopy(src, cur_len, message_group_byte, 0, part_len);
                //填充1个1和N个0
                message_group_byte[part_len] = (byte) 0x80;
                for(i = part_len + 1; i < 64; i++)
                {
                    message_group_byte[i] = 0x00;
                }
                //转换为小端模式
                byteToInt32_Little_endian(message_group_byte, message_group_int);
                //第一次数据分组转换
                msg_hash(hash_code, message_group_int);

                //填充0
                Arrays.fill(message_group_int, 0);
                //填充数据长度
                //受平台大小端模式影响，不要直接赋值，可移植性差
                //message_group_int[14] = total_len_bit << 3;
                //message_group_int[15] = 0;
                message_group_int[14] = ((byte)(total_len_bit & 0xff));
                message_group_int[14] |= ((byte)((total_len_bit >>> 8) & 0xff));
                message_group_int[14] |= ((byte)((total_len_bit >>> 16) & 0xff));
                message_group_int[14] |= ((byte)((total_len_bit >>> 24) & 0xff));
                message_group_int[15] = 0;
                //第二次数据分组转换
                msg_hash(hash_code, message_group_int);

                break;
            }
            else if(part_len == 64)
            {
                //分成两次分组转换

                //复制数据
                System.arraycopy(src, cur_len, message_group_byte, 0, part_len);
                //转换为小端模式
                byteToInt32_Little_endian(message_group_byte, message_group_int);
                //第一次数据分组转换
                msg_hash(hash_code, message_group_int);

                //填充1个1和N个0
                Arrays.fill(message_group_int, 0);
                message_group_int[0] = 0x80;
                //填充数据长度
                //受平台大小端模式影响，不要直接赋值，可移植性差
                //message_group_int[14] = total_len_bit << 3;
                //message_group_int[15] = 0;
                message_group_int[14] = ((byte)(total_len_bit & 0xff));
                message_group_int[14] |= ((byte)((total_len_bit >>> 8) & 0xff));
                message_group_int[14] |= ((byte)((total_len_bit >>> 16) & 0xff));
                message_group_int[14] |= ((byte)((total_len_bit >>> 24) & 0xff));
                message_group_int[15] = 0;
                //第二次数据分组转换
                msg_hash(hash_code, message_group_int);

                break;
            }
            else
            {
                //复制数据
                System.arraycopy(src, cur_len, message_group_byte, 0, part_len);
                //转换为小端模式
                byteToInt32_Little_endian(message_group_byte, message_group_int);
                //数据分组转换
                msg_hash(hash_code, message_group_int);
                cur_len += 64;
            }
        }while(true);

        return hash_code;
    }

    public static final int[] getMd5(String str)
    {
        int[] ret;
        ret = getMd5(str.getBytes());
        return ret;
    }

    public static final String FormatDecStr(int[] hash_code)
    {
        int i, j;
        int temp;
        String md5_str;

        md5_str = "";

        for(i = 0; i < 4; i++)
        {
            temp = hash_code[i] & 0xffff;
            for(j = 0; j < 5; j++)
            {
                md5_str = md5_str + (char)('0' + (temp % 10));
                temp /= 10;
            }
            temp = (hash_code[i] >>> 16);// & 0xffff;
            for(j = 0; j < 5; j++)
            {
                md5_str = md5_str + (char)('0' + (temp % 10));
                temp /= 10;
            }
        }
        return md5_str;
    }
}

class AuthoriseCode {

    public static final int AUTHORISE_TYPE_5MIN = 0;
    public static final int AUTHORISE_TYPE_1HOUR = 1;
    public static final int AUTHORISE_TYPE_2HOUR = 2;
    public static final int AUTHORISE_TYPE_3HOUR = 3;
    public static final int AUTHORISE_TYPE_1DAY = 4;
    public static final int AUTHORISE_TYPE_2DAY = 5;
    public static final int AUTHORISE_TYPE_3DAY = 6;

    public static String getAuthorisePassword(String adminPassword, int authoriseTyp, int year, int month, int monthDay, int hour, int minute)
    {
        int sendTime = getDays(year, month, monthDay);
        sendTime *= 1440;//24 * 60;
        sendTime += hour * 60;
        sendTime += minute;

        switch(authoriseTyp)
        {
            case AUTHORISE_TYPE_5MIN:
                break;
            case AUTHORISE_TYPE_1HOUR:
            case AUTHORISE_TYPE_2HOUR:
            case AUTHORISE_TYPE_3HOUR:
                sendTime = sendTime - sendTime % 10;//精确到十分钟
                break;
            case AUTHORISE_TYPE_1DAY:
            case AUTHORISE_TYPE_2DAY:
            case AUTHORISE_TYPE_3DAY:
                sendTime = sendTime - sendTime % 60;//精确到小时
                break;
            default:
                break;
        }

        String randomStr = String.format("%08x", sendTime);
        String new_password;
        String srcStr = adminPassword + authoriseTyp + randomStr;

        new_password = MD5.FormatDecStr(MD5.getMd5(srcStr));
        new_password = new_password.substring(0, 8);

        return new_password;
    }

    private static final int DAYS_400YEAR = 146097;		//(365*400 + 97)
    private static final int DAYS_100YEAR = 36524;		//(365*100 + 24)
    private static final int DAYS_100YEAR_LEAP = 36525;	//(365*100 + 25)
    private static final int DAYS_4YEAR = 1461;			//(365*4 + 1)

    private static int getDays(int year, int month, int monthDay)
    {
        int[] _daysOfMonth = new int[]{31,59,90,120,151,181,212,243,273,304,334,365};
        int[] _daysOfMonthLeapYear = new int[]{31,60,91,121,152,182,213,244,274,305,335,366};

        int ret = 0;
        int temp;
        int[] daysOfMonth;

        //基准年份必须为400的整数倍
        //以1600年为基准
        if(year >= 2000 && month > 0 && monthDay > 0)
        {
            year -= 2000;
            month--;
            monthDay--;

            temp = year / 400;
            ret += DAYS_400YEAR * temp;
            year %= 400;

            if(year >= 100)
            {
                ret += DAYS_100YEAR_LEAP;
                year -= 100;
                temp = year / 100;
                ret += DAYS_100YEAR * temp;
                year %= 100;
                if(temp > 0)
                    ret++;
            }

            temp = year / 4;
            ret += DAYS_4YEAR * temp;
            year %= 4;

            if(year > 0)
            {
                ret += 366 + (year - 1) * 365;
                daysOfMonth = _daysOfMonth;
            }
            else
            {
                daysOfMonth = _daysOfMonthLeapYear;
            }

            if(month > 0)
                ret += daysOfMonth[month - 1];
            ret += monthDay;
        }
        return ret;
    }
}

public class DynamicPasswordZC {
    public static void main(String[] args)
    {
        //指纹锁管理员密码
        //密码长度: 6至10位
        //字符范围: '0'~'9'
        //String adminPassword = "0123456789";
        String adminPassword = "123456";

        //动态密码有效期
        //AuthoriseCode.AUTHORISE_TYPE_5MIN;
        //AuthoriseCode.AUTHORISE_TYPE_1HOUR;
        //AuthoriseCode.AUTHORISE_TYPE_2HOUR;
        //AuthoriseCode.AUTHORISE_TYPE_3HOUR;
        //AuthoriseCode.AUTHORISE_TYPE_1DAY;
        //AuthoriseCode.AUTHORISE_TYPE_2DAY;
        //AuthoriseCode.AUTHORISE_TYPE_3DAY;
        int authoriseTyp = AuthoriseCode.AUTHORISE_TYPE_1HOUR;

        //当前时间: 2018-7-3 11:54
        int year = 2018;
        int month = 7;
        int monthDay = 3;
        int hour = 11;
        int minute = 54;

        //动态开锁密码
        String psd = AuthoriseCode.getAuthorisePassword(adminPassword, authoriseTyp, year, month, monthDay, hour, minute);
        System.out.println(psd);
    }
}

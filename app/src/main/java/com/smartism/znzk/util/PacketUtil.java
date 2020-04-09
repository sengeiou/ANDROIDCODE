package com.smartism.znzk.util;

public class PacketUtil {
	private static String HexCode[] = { "0", "1", "2", "3", "4", "5", "6", "7",
			"8", "9", "A", "B", "C", "D", "E", "F" };

	private PacketUtil() {
	}

	public static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return HexCode[d1] + HexCode[d2];
	}

	public static String byteArrayToHexString(byte b[]) {
		String result = "";
		for (int i = 0; i < b.length; i++)
			result = result + byteToHexString(b[i]);
		return result;
	}

	public static int byte2int(byte b[], int offset) {
		return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8
				| (b[offset + 1] & 0xff) << 16 | (b[offset] & 0xff) << 24;
	}

	public static int byte2int(byte b[]) {
		return b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16
				| (b[0] & 0xff) << 24;
	}

	public static long byte2long(byte b[]) {
		return (long) b[7] & (long) 255 | ((long) b[6] & (long) 255) << 8
				| ((long) b[5] & (long) 255) << 16
				| ((long) b[4] & (long) 255) << 24
				| ((long) b[3] & (long) 255) << 32
				| ((long) b[2] & (long) 255) << 40
				| ((long) b[1] & (long) 255) << 48 | (long) b[0] << 56;
	}

	public static long byte2long(byte b[], int offset) {
		return (long) b[offset + 7] & (long) 255
				| ((long) b[offset + 6] & (long) 255) << 8
				| ((long) b[offset + 5] & (long) 255) << 16
				| ((long) b[offset + 4] & (long) 255) << 24
				| ((long) b[offset + 3] & (long) 255) << 32
				| ((long) b[offset + 2] & (long) 255) << 40
				| ((long) b[offset + 1] & (long) 255) << 48
				| (long) b[offset] << 56;
	}

	public static byte[] int2byte(int n) {
		byte b[] = new byte[4];
		b[0] = (byte) (n >> 24);
		b[1] = (byte) (n >> 16);
		b[2] = (byte) (n >> 8);
		b[3] = (byte) n;
		return b;
	}

	public static void int2byte(int n, byte buf[], int offset) {
		buf[offset] = (byte) (n >> 24);
		buf[offset + 1] = (byte) (n >> 16);
		buf[offset + 2] = (byte) (n >> 8);
		buf[offset + 3] = (byte) n;
	}

	public static byte[] short2byte(int n) {
		byte b[] = new byte[2];
		b[0] = (byte) (n >> 8);
		b[1] = (byte) n;
		return b;
	}

	public static void short2byte(int n, byte buf[], int offset) {
		buf[offset] = (byte) (n >> 8);
		buf[offset + 1] = (byte) n;
	}

	public static byte[] long2byte(long n) {
		byte b[] = new byte[8];
		b[0] = (byte) (int) (n >> 56);
		b[1] = (byte) (int) (n >> 48);
		b[2] = (byte) (int) (n >> 40);
		b[3] = (byte) (int) (n >> 32);
		b[4] = (byte) (int) (n >> 24);
		b[5] = (byte) (int) (n >> 16);
		b[6] = (byte) (int) (n >> 8);
		b[7] = (byte) (int) n;
		return b;
	}

	public static void long2byte(long n, byte buf[], int offset) {
		buf[offset] = (byte) (int) (n >> 56);
		buf[offset + 1] = (byte) (int) (n >> 48);
		buf[offset + 2] = (byte) (int) (n >> 40);
		buf[offset + 3] = (byte) (int) (n >> 32);
		buf[offset + 4] = (byte) (int) (n >> 24);
		buf[offset + 5] = (byte) (int) (n >> 16);
		buf[offset + 6] = (byte) (int) (n >> 8);
		buf[offset + 7] = (byte) (int) n;
	}

	/**
	 * 将整数转换为16进制字符串，length > 0时将判断转换后的长度，不够前面补0
	 *
	 * @param n
	 * @param length
	 */
	public static String long2HexString(long n, int length) {
		StringBuilder result = new StringBuilder();
		result.append(Long.toHexString(n));
		if (length > 0 && result.length() < length) {
			while (result.length() < length) {
				result.insert(0, "0");
			}
		}
		return result.toString();
	}

	public static boolean checkMobile(String sMobile) {
		String sF6 = "", sB7 = "", sF2 = "";
		if (sMobile == null)
			return false;
		if (sMobile.length() != 11)
			return false;
		sF6 = sMobile.substring(0, 7);
		sF2 = sMobile.substring(0, 2);
		sB7 = sMobile.substring(7);
		try {
			Integer.valueOf(sF6).intValue();
			Integer.valueOf(sB7).intValue();
			if (sF2.equals("13"))
				return true;
			else
				return false;
		} catch (Exception ex) {
			return false;
		}
	}
	public static byte[] HexString2Bytes(String src) {
		if (src==null) {
			return null;
		}
		if (src.length() % 2 != 0) {
			src = "0" + src;
		}
		byte[] ret = new byte[src.length()/2];
		byte[] tmp = src.getBytes();
		for (int i = 0; i < src.length()/2; ++i) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
		}
		return ret;
	}
	private static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
				.byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
				.byteValue();
		byte ret = (byte) (_b0 | _b1);
		return ret;
	}

	/**
	 * 将整数转换为16进制字符串，length > 0时将判断转换后的长度，不够前面补0
	 *
	 * @param n
	 * @param length
	 */
	public static String int2HexString(int n, int length) {
		StringBuilder result = new StringBuilder();
		result.append(Integer.toHexString(n));
		if (length > 0 && result.length() < length) {
			while (result.length() < length) {
				result.insert(0, "0");
			}
		}
		return result.toString();
	}
}

package com.ilsian.rmweb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Util {
	public static void safeClose(PreparedStatement ps) {
		if (ps != null)
			try {ps.close();} catch (SQLException ignore) {}	
	}
	public static void safeClose(ResultSet ps) {
		if (ps != null)
			try {ps.close();} catch (SQLException ignore) {}	
	}
}

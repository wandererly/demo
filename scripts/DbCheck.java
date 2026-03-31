import java.sql.*;
public class DbCheck {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:mysql://localhost:3306/hrm?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    String user = "root";
    String pass = "041029";
    try (Connection c = DriverManager.getConnection(url, user, pass)) {
      PreparedStatement ps = c.prepareStatement("select username, password_hash from sys_user where username=?");
      ps.setString(1, "admin");
      ResultSet rs = ps.executeQuery();
      if (!rs.next()) { System.out.println("no user"); return; }
      String hash = rs.getString(2);
      System.out.println("hash=" + hash);
      org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
      System.out.println("matches=" + enc.matches("admin123", hash));
    }
  }
}

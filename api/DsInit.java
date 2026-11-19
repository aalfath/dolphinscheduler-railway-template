// Small JDBC helper for the DolphinScheduler Railway template.
//   wait-db      : wait until PostgreSQL accepts connections
//   wait-schema  : wait until the DolphinScheduler schema exists (t_ds_version has a row)
//   admin-password: replace the upstream default admin password with $ADMIN_PASSWORD,
//                   only while the default is still set (a password changed in the UI is kept)
import java.sql.*;

public class DsInit {
    static final String DEFAULT_ADMIN_MD5 = "7ad2410b2f4c074479a8937a28a22b8f"; // md5("dolphinscheduler123")

    static Connection connect() throws SQLException {
        return DriverManager.getConnection(env("SPRING_DATASOURCE_URL"),
                env("SPRING_DATASOURCE_USERNAME"), env("SPRING_DATASOURCE_PASSWORD"));
    }

    static String env(String k) {
        String v = System.getenv(k);
        if (v == null || v.isEmpty()) { System.err.println("[ds-init] " + k + " is not set"); System.exit(2); }
        return v;
    }

    public static void main(String[] a) throws Exception {
        String mode = a.length > 0 ? a[0] : "";
        int timeout = Integer.parseInt(System.getenv().getOrDefault("DS_INIT_TIMEOUT", "900"));
        long end = System.currentTimeMillis() + timeout * 1000L;
        String last = "";
        while (true) {
            try (Connection c = connect()) {
                if (mode.equals("wait-db")) { System.out.println("[ds-init] database is reachable"); return; }
                if (mode.equals("wait-schema")) {
                    try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT version FROM t_ds_version LIMIT 1")) {
                        if (r.next()) { System.out.println("[ds-init] schema version " + r.getString(1)); return; }
                    }
                    throw new SQLException("schema has no version row yet");
                }
                if (mode.equals("admin-password")) {
                    String pw = System.getenv("ADMIN_PASSWORD");
                    if (pw == null || pw.isEmpty()) { System.out.println("[ds-init] ADMIN_PASSWORD not set, admin password left unchanged"); return; }
                    try (PreparedStatement p = c.prepareStatement(
                            "UPDATE t_ds_user SET user_password = md5(?), update_time = now() WHERE user_name = 'admin' AND user_password = ?")) {
                        p.setString(1, pw);
                        p.setString(2, DEFAULT_ADMIN_MD5);
                        int n = p.executeUpdate();
                        System.out.println(n > 0 ? "[ds-init] default admin password replaced with ADMIN_PASSWORD"
                                                 : "[ds-init] admin password is not the upstream default, left unchanged");
                    }
                    return;
                }
                System.err.println("usage: DsInit wait-db|wait-schema|admin-password"); System.exit(2);
            } catch (SQLException e) {
                if (!e.getMessage().equals(last)) { System.out.println("[ds-init] waiting (" + mode + "): " + e.getMessage()); last = e.getMessage(); }
                if (System.currentTimeMillis() > end) { System.err.println("[ds-init] gave up after " + timeout + "s"); System.exit(1); }
                Thread.sleep(3000);
            }
        }
    }
}

// Small JDBC helper for the DolphinScheduler Railway template.
//   schema       : wait for PostgreSQL, then create or upgrade the DolphinScheduler schema under a
//                  Postgres advisory lock (so API, master and alert server can all start at once);
//                  skipped when t_ds_version already matches this image's version
//   host         : print DolphinScheduler's own NetUtils.getHost() (needs the DS libs on the classpath)
//   admin-password: replace the upstream default admin password with $ADMIN_PASSWORD,
//                   only while the default is still set (a password changed in the UI is kept)
import java.nio.file.*;
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

    static String currentVersion(Connection c) {
        try (Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT version FROM t_ds_version ORDER BY id DESC LIMIT 1")) {
            return r.next() ? r.getString(1) : null;
        } catch (SQLException e) {
            return null;
        }
    }

    static void schema(Connection c) throws Exception {
        String want = new String(Files.readAllBytes(Paths.get("/opt/dolphinscheduler/tools/sql/sql/soft_version"))).trim();
        c.setAutoCommit(true);
        try (Statement s = c.createStatement()) { s.execute("SELECT pg_advisory_lock(2685001)"); }
        try {
            String have = currentVersion(c);
            if (want.equals(have)) { System.out.println("[ds-init] schema is at " + have + ", nothing to do"); return; }
            System.out.println("[ds-init] schema is at " + (have == null ? "nothing" : have) + ", running upgrade-schema.sh to " + want);
            Process p = new ProcessBuilder("bash", "/opt/dolphinscheduler/tools/bin/upgrade-schema.sh").inheritIO().start();
            int rc = p.waitFor();
            if (rc != 0 || !want.equals(currentVersion(c))) { System.err.println("[ds-init] schema upgrade failed (exit " + rc + ")"); System.exit(1); }
            System.out.println("[ds-init] schema is now at " + want);
        } finally {
            try (Statement s = c.createStatement()) { s.execute("SELECT pg_advisory_unlock(2685001)"); }
        }
    }

    public static void main(String[] a) throws Exception {
        String mode = a.length > 0 ? a[0] : "";
        if (mode.equals("host")) {
            // Same address DolphinScheduler puts in its heartbeats (unbracketed for IPv6).
            Object h = Class.forName("org.apache.dolphinscheduler.common.utils.NetUtils").getMethod("getHost").invoke(null);
            System.out.println("DSHOST=" + h);
            return;
        }
        int timeout = Integer.parseInt(System.getenv().getOrDefault("DS_INIT_TIMEOUT", "900"));
        long end = System.currentTimeMillis() + timeout * 1000L;
        String last = "";
        while (true) {
            try (Connection c = connect()) {
                if (mode.equals("schema")) { schema(c); return; }
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
                System.err.println("usage: DsInit schema|host|admin-password"); System.exit(2);
            } catch (SQLException e) {
                if (!e.getMessage().equals(last)) { System.out.println("[ds-init] waiting (" + mode + "): " + e.getMessage()); last = e.getMessage(); }
                if (System.currentTimeMillis() > end) { System.err.println("[ds-init] gave up after " + timeout + "s"); System.exit(1); }
                Thread.sleep(3000);
            }
        }
    }
}

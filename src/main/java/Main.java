import javax.swing.SwingUtilities;

/** Desktop entry point: all UI creation is confined to the Swing EDT. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        if (args != null && args.length == 1 && "--version".equals(args[0])) {
            System.out.println("AirRaid " + AppVersion.NUMBER + " / " + AppVersion.CHANNEL);
            return;
        }
        if (args != null && args.length == 1 && "--self-test".equals(args[0])) {
            try {
                // Headless package smoke: prove native launcher, embedded JVM and
                // classpath media are usable without opening a desktop window.
                if (Assets.image("/Resource/General/background.png").getWidth(null) <= 0
                        || Assets.image("/Resource/Window/gameIcon.jpg").getWidth(null) <= 0) {
                    throw new IllegalStateException("Invalid bundled images");
                }
                Assets.font("/Resource/Font/Pixel.ttf", 22f);
                GameModel model = new GameModel();
                model.startNewGame();
                model.tick(16);
                if (model.state() != GameState.RUNNING
                        || model.timeRemainingMs() != GameRules.MATCH_DURATION_MS - 16) {
                    throw new IllegalStateException("Unexpected model lifecycle");
                }
                System.out.println("AIRRAID_PACKAGE_SELFTEST_PASS");
            } catch (RuntimeException failure) {
                AppDiagnostics.report(AppDiagnostics.FailureCode.RESOURCES);
                System.exit(1);
            }
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                new Window().showWindow();
            } catch (RuntimeException exception) {
                AppDiagnostics.reportStartup();
            }
        });
    }
}

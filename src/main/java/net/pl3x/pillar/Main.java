package net.pl3x.pillar;

public final class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalStateException("Invalid syntax! Missing args");
        }

        String project = args[0].trim();
        String version = args[1].trim();
        final int build;

        if (args.length > 2) {
            try {
                build = Integer.parseInt(args[2].trim());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid build number!");
            }
            if (build < 1) {
                throw new IllegalStateException("Invalid build number!");
            }
        } else {
            build = -1;
        }

        Pillar pillar = new Pillar(project, version, build);

        pillar.run();
    }
}

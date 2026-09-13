package FusionPlanet.content;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.util.Log;
import arc.util.Time;
import mindustry.game.EventType.ContentInitEvent;
import mindustry.game.Team;

import java.lang.reflect.Field;

public class PrecursorTeam {

    public static Team get() {
        return Team.get(7);
    }

    public static int getId() {
        return 7;
    }

    public static Color getColor() {
        return Color.valueOf("E6E6E6");
    }

    public static void load() {
        Events.on(ContentInitEvent.class, e -> {
            Team sharded = Team.get(1);
            if (sharded != null) {
                sharded.color.set(Color.valueOf("BFA25F"));
                sharded.setPalette(
                        Color.valueOf("BFA25F"),
                        Color.valueOf("8F7E56"),
                        Color.valueOf("5F5439")
                );
                Log.info("Sharded team color set to BFA25F");
            }

            Team crux = Team.get(2);
            if (crux != null) {
                crux.color.set(Color.valueOf("B33E47"));
                crux.setPalette(
                        Color.valueOf("B33E47"),
                        Color.valueOf("8F3139"),
                        Color.valueOf("5F2126")
                );
                Log.info("Crux team color set to B33E47");
            }

            Team precursor = Team.get(7);
            if (precursor != null) {
                precursor.name = "precursor";
                precursor.color.set(Color.valueOf("E6E6E6"));
                precursor.setPalette(
                        Color.valueOf("E6E6E6"),
                        Color.valueOf("ACACAC"),
                        Color.valueOf("737373")
                );
                Log.info("Team 7 set to precursor (E6E6E6)");

                try {
                    Field field = Team.class.getDeclaredField("baseTeams");
                    field.setAccessible(true);

                    Team[] oldArr = (Team[]) field.get(null);
                    if (oldArr == null) {
                        Log.err("[precursor] baseTeams is null");
                    } else {
                        Log.info("[precursor] baseTeams old length = " + oldArr.length);

                        boolean found = false;
                        for (Team t : oldArr) {
                            if (t == precursor) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            Team[] newArr = new Team[oldArr.length + 1];
                            System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                            newArr[oldArr.length] = precursor;
                            field.set(null, newArr);

                            Team[] checkArr = (Team[]) field.get(null);
                            if (checkArr != null && checkArr.length == oldArr.length + 1) {
                                Log.info("[precursor] baseTeams extended successfully!");
                            } else {
                                Log.err("[precursor] baseTeams NOT extended (JVM refused).");
                            }
                        } else {
                            Log.info("[precursor] precursor already in baseTeams");
                        }
                    }
                } catch (Exception ex) {
                    Log.err("[precursor] reflection failed: " + ex);
                }
            }
        });

        Events.on(ContentInitEvent.class, e -> Time.run(10f, () -> {
            Team t = Team.get(7);
            if (t != null) {
                Log.info(String.format(
                        "Precursor color: R=%.3f G=%.3f B=%.3f name=%s",
                        t.color.r, t.color.g, t.color.b, t.name
                ));
            }
        }));

        Events.on(ContentInitEvent.class, e -> Time.run(20f, () -> {
            if (Core.atlas.has("team-precursor")) {
                Log.info("[precursor] icon found: team-precursor");
            } else {
                Log.err("[precursor] icon NOT found in atlas (name: team-precursor)");
            }

            if (Core.atlas.has("team-sharded")) {
                Log.info("[precursor] reference: team-sharded found OK");
            }
        }));
    }
}
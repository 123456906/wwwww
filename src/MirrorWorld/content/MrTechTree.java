package MirrorWorld.content;

import mindustry.content.TechTree;

import static MirrorWorld.content.MirrorSystemPlanets.*;
import static MirrorWorld.content.MrBlocks.*;
import static MirrorWorld.content.MrItems.*;
import static MirrorWorld.content.MrUnits.*;
import static mindustry.type.ItemStack.with;

/**
 * 反地球科技树。
 *
 * 除根核心 core-echo 外，模组方块已取消 alwaysUnlocked，必须通过本科技树解锁；
 * 节点需求统一使用 MrItems 的镜潮材料。
 */
public class MrTechTree {

    public static void load() {
        antichthon.techTree = TechTree.nodeRoot("antichthon", coreEcho, () -> {

            /* ================= 核心 ================= */
            TechTree.node(coreEvoke, with(mirrorShard, 1500, facetPlate, 800, prismCrystal, 400), () -> {
                TechTree.node(coreMirror, with(prismCrystal, 2500, mirrorAlloy, 1200, resonanceCell, 300), () -> {});
            });

            TechTree.node(assimilator, with(facetPlate, 800, prismCrystal, 600, echoChip, 200), () -> {});
            TechTree.node(summonTurret, with(mirrorShard, 600, facetPlate, 400, prismCrystal, 300), () -> {});

            /* ================= 防御 ================= */
            TechTree.node(shardWall, with(mirrorShard, 60), () -> {
                TechTree.node(shardWallLarge, with(mirrorShard, 300), () -> {});
                TechTree.node(shardDoor, with(mirrorShard, 150), () -> {});

                TechTree.node(facetWall, with(facetPlate, 100), () -> {
                    TechTree.node(facetWallLarge, with(facetPlate, 500), () -> {});
                    TechTree.node(facetDoor, with(facetPlate, 250), () -> {});
                    TechTree.node(mirrorWall, with(mirrorAlloy, 150), () -> {});

                    TechTree.node(prismWall, with(prismCrystal, 200), () -> {
                        TechTree.node(prismWallLarge, with(prismCrystal, 900), () -> {});
                    });

                    TechTree.node(veilWall, with(veilFiber, 250), () -> {
                        TechTree.node(veilWallLarge, with(veilFiber, 1000), () -> {});
                    });
                });
            });

            /* ================= 钻头 ================= */
            TechTree.node(echoDrill, with(mirrorShard, 100, facetPlate, 80), () -> {
                TechTree.node(prismDrill, with(prismCrystal, 150), () -> {
                    TechTree.node(lumenDrill, with(lumenCrystal, 250), () -> {});
                });
                TechTree.node(mirrorBore, with(mirrorAlloy, 300), () -> {});
                TechTree.node(abyssBore, with(voidOre, 400, resonantAlloy, 200), () -> {});
            });

            /* ================= 物流 ================= */
            TechTree.node(mirageConveyor, with(mirrorShard, 30), () -> {
                TechTree.node(prismConveyor, with(prismCrystal, 60), () -> {});
                TechTree.node(shardJunction, with(mirrorShard, 40), () -> {});
                TechTree.node(facetRouter, with(facetPlate, 60), () -> {});
                TechTree.node(facetSorter, with(echoChip, 80), () -> {});
                TechTree.node(echoBridge, with(echoChip, 120), () -> {});
                TechTree.node(mirrorBridge, with(mirrorAlloy, 150), () -> {});
            });

            TechTree.node(tideConduit, with(facetPlate, 50), () -> {
                TechTree.node(prismConduit, with(prismCrystal, 80), () -> {});
                TechTree.node(tideBridge, with(mirrorAlloy, 100), () -> {});
                TechTree.node(echoPump, with(facetPlate, 80, echoChip, 40), () -> {});
                TechTree.node(abyssPump, with(voidOre, 200, resonantAlloy, 100), () -> {});
            });

            /* ================= 电力 ================= */
            TechTree.node(lumenNode, with(mirrorShard, 100, facetPlate, 80), () -> {
                TechTree.node(lumenTower, with(lumenCrystal, 200), () -> {});
                TechTree.node(shardBattery, with(mirrorShard, 150), () -> {});
                TechTree.node(prismBattery, with(prismCrystal, 250), () -> {});
                TechTree.node(glowPanel, with(lumenCrystal, 200), () -> {});
                TechTree.node(auroraPanel, with(prismLens, 200), () -> {});

                TechTree.node(emberVent, with(facetPlate, 200), () -> {
                    TechTree.node(emberGenerator, with(mirrorAlloy, 300), () -> {});
                });

                TechTree.node(voidReactor, with(voidOre, 600, resonantAlloy, 400, resonanceCell, 200), () -> {});
            });

            /* ================= 制造 ================= */
            TechTree.node(shardKiln, with(mirrorShard, 120), () -> {
                TechTree.node(facetPress, with(mirrorShard, 200), () -> {
                    TechTree.node(prismSmelter, with(prismCrystal, 300), () -> {});
                    TechTree.node(crystalCrucible, with(prismCrystal, 500, facetPlate, 300), () -> {});
                    TechTree.node(mirrorFoundry, with(lumenCrystal, 400, prismCrystal, 300), () -> {});
                    TechTree.node(pyraMixer, with(prismLens, 300), () -> {});
                    TechTree.node(cryoMixer, with(lumenCrystal, 300), () -> {});
                    TechTree.node(facetMixer, with(prismCrystal, 500, lumenCrystal, 400, facetPlate, 300), () -> {});
                    TechTree.node(facetSeparator, with(echoChip, 300, mirrorAlloy, 200), () -> {});

                    TechTree.node(alloyForge, with(facetPlate, 800, prismCrystal, 600, lumenCrystal, 400, phaseGlass, 200), () -> {
                        TechTree.node(veilWeaver, with(mirrorAlloy, 500, lumenCrystal, 400, phaseGlass, 300, prismLens, 200), () -> {});
                    });
                });
            });

            /* ================= 流体生产 ================= */
            TechTree.node(mirrorStill, with(facetPlate, 300, echoChip, 100), () -> {});
            TechTree.node(lumenLeach, with(lumenCrystal, 300), () -> {});
            TechTree.node(voidRefinery, with(voidOre, 300, mirrorAlloy, 200), () -> {});
            TechTree.node(mirrorMelter, with(mirrorAlloy, 300), () -> {});
            TechTree.node(phaseDissolver, with(phaseGlass, 300, echoChip, 150), () -> {});
            TechTree.node(echoReactor, with(resonanceCell, 300, resonantAlloy, 200), () -> {});

            /* ================= 存储 ================= */
            TechTree.node(echoContainer, with(mirrorShard, 200), () -> {
                TechTree.node(mirrorVault, with(mirrorAlloy, 400), () -> {
                    TechTree.node(abyssVault, with(abyssCore, 200, resonantAlloy, 300), () -> {});
                });
            });

            /* ================= 效果 ================= */
            TechTree.node(veilNode, with(veilFiber, 150), () -> {});
            TechTree.node(haloProjector, with(prismLens, 250), () -> {});
            TechTree.node(auroraDome, with(phaseGlass, 300, echoChip, 200), () -> {});
            TechTree.node(mirrorShield, with(mirrorAlloy, 400, veilFiber, 200), () -> {});
            TechTree.node(shardMine, with(mirrorShard, 100, prismCrystal, 80), () -> {});

            /* ================= 炮塔 ================= */
            TechTree.node(glintTurret, with(mirrorShard, 200), () -> {
                TechTree.node(shardScatter, with(prismCrystal, 250), () -> {});
                TechTree.node(facetLancer, with(prismCrystal, 300, mirrorAlloy, 100), () -> {});
                TechTree.node(mirageArc, with(mirrorAlloy, 300, echoChip, 100), () -> {});
                TechTree.node(echoRipple, with(echoChip, 250, prismLens, 100), () -> {});
                TechTree.node(prismCyclone, with(prismLens, 300, phaseGlass, 150), () -> {});
                TechTree.node(abyssLance, with(voidOre, 500, resonantAlloy, 200), () -> {});
                TechTree.node(haloBeam, with(prismLens, 400, echoChip, 300, resonantAlloy, 250), () -> {});
                TechTree.node(veilSegment, with(veilFiber, 200, mirrorAlloy, 150), () -> {});
                TechTree.node(prismRepair, with(prismCrystal, 200, phaseGlass, 100), () -> {});
            });

            /* ================= 单位生产 ================= */
            TechTree.node(shardAssembler, with(mirrorShard, 150, facetPlate, 200), () -> {
                TechTree.node(mite, with(facetPlate, 30), () -> {});
                TechTree.node(crawler, with(mirrorShard, 40, prismCrystal, 30), () -> {});
                TechTree.node(dagger, with(prismCrystal, 50, facetPlate, 30), () -> {});

                TechTree.node(facetUpgrader, with(prismCrystal, 400, prismLens, 200), () -> {
                    TechTree.node(nomad, with(prismCrystal, 80, mirrorAlloy, 40), () -> {});
                    TechTree.node(sabre, with(prismCrystal, 100, mirrorAlloy, 60), () -> {});
                    TechTree.node(mace, with(prismCrystal, 120, mirrorAlloy, 80), () -> {});

                    TechTree.node(prismUpgrader, with(mirrorAlloy, 600, lumenCrystal, 400), () -> {
                        TechTree.node(mender, with(phaseGlass, 100, prismLens, 80), () -> {});
                        TechTree.node(buster, with(phaseGlass, 150, mirrorAlloy, 100), () -> {});
                        TechTree.node(aegis, with(phaseGlass, 200, resonanceCell, 80), () -> {});

                        TechTree.node(abyssReconstructor, with(abyssCore, 400, resonantAlloy, 600, echoChip, 500), () -> {
                            TechTree.node(leviathan, with(abyssCore, 150, resonantAlloy, 200), () -> {});
                            TechTree.node(ark, with(abyssCore, 250, resonantAlloy, 300), () -> {});
                            TechTree.node(skylift, with(abyssCore, 200, veilFiber, 200), () -> {});
                        });
                    });
                });
            });

            TechTree.node(skyForge, with(prismCrystal, 300, mirrorAlloy, 200), () -> {
                TechTree.node(vulture, with(prismCrystal, 80), () -> {});
                TechTree.node(wyvern, with(prismCrystal, 150, mirrorAlloy, 80), () -> {});
                TechTree.node(specter, with(prismCrystal, 250, mirrorAlloy, 150), () -> {});
            });

            TechTree.node(abyssFoundry, with(voidOre, 700, veilFiber, 400, abyssCore, 300), () -> {});

            TechTree.node(falcon, with(prismCrystal, 200, mirrorAlloy, 100), () -> {});
            TechTree.node(nova, with(mirrorShard, 200, facetPlate, 100), () -> {});
            TechTree.node(envoy, with(prismCrystal, 180, echoChip, 100), () -> {});
            TechTree.node(cargo, with(facetPlate, 150, echoChip, 80), () -> {});

            /* ================= 物品图鉴 ================= */
            TechTree.nodeProduce(mirrorShard, () -> {
                TechTree.nodeProduce(facetPlate, () -> {});

                TechTree.nodeProduce(prismCrystal, () -> {
                    TechTree.nodeProduce(lumenCrystal, () -> {});
                    TechTree.nodeProduce(prismLens, () -> {});
                    TechTree.nodeProduce(phaseGlass, () -> {});
                    TechTree.nodeProduce(echoChip, () -> {});
                });

                TechTree.nodeProduce(voidOre, () -> {
                    TechTree.nodeProduce(abyssOre, () -> {});
                    TechTree.nodeProduce(mirrorAlloy, () -> {});
                    TechTree.nodeProduce(lumenThread, () -> {});
                    TechTree.nodeProduce(veilFiber, () -> {});
                    TechTree.nodeProduce(resonanceCell, () -> {});
                    TechTree.nodeProduce(resonantAlloy, () -> {});
                    TechTree.nodeProduce(abyssCore, () -> {});
                });
            });
        });
    }
}

package exiledsector.skills.skilleffect;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector2f;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SplitBeamEffectsTest {

    private MockedStatic<Global> globalMock;
    private BeamAPI sourceBeam;
    private ShipAPI splitTarget;

    @BeforeEach
    void setUp() {
        CombatEngineAPI engine = mock(CombatEngineAPI.class);
        globalMock = Mockito.mockStatic(Global.class);
        globalMock.when(Global::getCombatEngine).thenReturn(engine);
        sourceBeam = mock(BeamAPI.class);
        splitTarget = mock(ShipAPI.class);
        when(splitTarget.isAlive()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        globalMock.close();
    }

    private void giveWeaponBeamEffect(BeamEffectPlugin prototype) {
        WeaponAPI weapon = mock(WeaponAPI.class);
        BeamWeaponSpecAPI spec = mock(BeamWeaponSpecAPI.class);
        when(weapon.getSpec()).thenReturn(spec);
        when(spec.getBeamEffect()).thenReturn(prototype);
        when(sourceBeam.getWeapon()).thenReturn(weapon);
    }

    @Test
    void splitBeamRedirectsTargetAndGeometryButDelegatesEverythingElse() {
        DamageAPI damage = mock(DamageAPI.class);
        ShipAPI primaryTarget = mock(ShipAPI.class);
        when(sourceBeam.getDamage()).thenReturn(damage);
        when(sourceBeam.getBrightness()).thenReturn(1f);
        when(sourceBeam.getDamageTarget()).thenReturn(primaryTarget);

        BeamAPI split = SplitBeamEffects.splitBeam(sourceBeam, splitTarget, new Vector2f(0f, 0f), new Vector2f(30f, 40f));

        assertSame(splitTarget, split.getDamageTarget());
        assertEquals(new Vector2f(0f, 0f), split.getFrom());
        assertEquals(new Vector2f(30f, 40f), split.getTo());
        assertEquals(new Vector2f(30f, 40f), split.getRayEndPrevFrame());
        assertEquals(50f, split.getLength(), 0.001f);
        assertSame(damage, split.getDamage());
        assertEquals(1f, split.getBrightness());
    }

    @Test
    void splitBeamSettersNeverTouchTheRealBeam() {
        BeamAPI split = SplitBeamEffects.splitBeam(sourceBeam, splitTarget, new Vector2f(), new Vector2f());

        split.setWidth(99f);
        split.setCoreColor(Color.RED);

        verify(sourceBeam, never()).setWidth(99f);
        verify(sourceBeam, never()).setCoreColor(any());
    }

    @Test
    void createsAFreshPluginInstancePerSplitRatherThanSharingTheWeaponsOwn() {
        RecordingPlugin prototype = new RecordingPlugin();
        giveWeaponBeamEffect(prototype);

        BeamEffectPlugin created = SplitBeamEffects.newPluginFor(sourceBeam.getWeapon());

        assertInstanceOf(RecordingPlugin.class, created);
        assertNotSame(prototype, created);
    }

    @Test
    void weaponsWithoutABeamEffectGetNoPlugin() {
        WeaponAPI weaponWithoutEffect = mock(WeaponAPI.class);
        BeamWeaponSpecAPI specWithoutEffect = mock(BeamWeaponSpecAPI.class);
        when(weaponWithoutEffect.getSpec()).thenReturn(specWithoutEffect);
        WeaponAPI nonBeamWeapon = mock(WeaponAPI.class);

        assertNull(SplitBeamEffects.newPluginFor(weaponWithoutEffect));
        assertNull(SplitBeamEffects.newPluginFor(nonBeamWeapon));
    }

    @Test
    void pluginsThatNeedConstructorArgumentsAreSkipped() {
        giveWeaponBeamEffect(new NeedsArgumentsPlugin("x"));

        assertNull(SplitBeamEffects.newPluginFor(sourceBeam.getWeapon()));
    }

    @Test
    void runsTheSamePluginEveryFrameAgainstTheSplitBeamUntilItTimesOut() {
        giveWeaponBeamEffect(new FrameCountingPlugin());
        FrameCountingPlugin.reset();
        SplitBeamEffects effects = new SplitBeamEffects();

        effects.refresh(sourceBeam, splitTarget, new Vector2f(), new Vector2f(10f, 0f));
        effects.advance(0.1f);
        effects.advance(0.1f);
        effects.refresh(sourceBeam, splitTarget, new Vector2f(), new Vector2f(10f, 0f));
        effects.advance(0.1f);
        effects.advance(1f);
        effects.advance(0.1f);

        assertEquals(3, FrameCountingPlugin.targetsSeen.size());
        assertTrue(FrameCountingPlugin.targetsSeen.stream().allMatch(target -> target == splitTarget));
        assertEquals(1, FrameCountingPlugin.instances);
    }

    @Test
    void stopsRunningForADeadSplitTarget() {
        giveWeaponBeamEffect(new FrameCountingPlugin());
        FrameCountingPlugin.reset();
        SplitBeamEffects effects = new SplitBeamEffects();
        effects.refresh(sourceBeam, splitTarget, new Vector2f(), new Vector2f());

        when(splitTarget.isAlive()).thenReturn(false);
        effects.advance(0.1f);

        assertTrue(FrameCountingPlugin.targetsSeen.isEmpty());
    }

    @Test
    void aPluginThatFailsOnASplitBeamIsDisabledForSplitBeamsFromThenOn() {
        ThrowingPlugin.calls = 0;
        giveWeaponBeamEffect(new ThrowingPlugin());
        SplitBeamEffects effects = new SplitBeamEffects();

        effects.refresh(sourceBeam, splitTarget, new Vector2f(), new Vector2f());
        effects.advance(0.1f);
        effects.refresh(sourceBeam, splitTarget, new Vector2f(), new Vector2f());
        effects.advance(0.1f);

        assertEquals(1, ThrowingPlugin.calls);
        assertNull(SplitBeamEffects.newPluginFor(sourceBeam.getWeapon()));
    }

    public static class RecordingPlugin implements BeamEffectPlugin {
        @Override
        public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
            beam.getDamageTarget();
        }
    }

    public static class NeedsArgumentsPlugin implements BeamEffectPlugin {
        private final String argument;

        public NeedsArgumentsPlugin(String argument) {
            this.argument = argument;
        }

        @Override
        public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
            beam.getDamageTarget().setCustomData(argument, argument);
        }
    }

    public static class FrameCountingPlugin implements BeamEffectPlugin {
        static final List<Object> targetsSeen = new ArrayList<>();
        static int instances;

        public FrameCountingPlugin() {
            instances++;
        }

        static void reset() {
            targetsSeen.clear();
            instances = 0;
        }

        @Override
        public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
            targetsSeen.add(beam.getDamageTarget());
        }
    }

    public static class ThrowingPlugin implements BeamEffectPlugin {
        static int calls;

        @Override
        public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
            calls++;
            throw new ClassCastException("casts BeamAPI to an internal class");
        }
    }
}

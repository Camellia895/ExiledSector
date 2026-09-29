package exiledsector.skills.skilleffect;

import exiledsector.i18n.NumberText;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberTextParityTest {

    @Test
    void formatsNumbersExactlyLikeTheOldPercentHelper() {
        Random random = new Random(42);
        float[] fixed = {0f, -0f, 1f, -1f, 0.5f, 12.5f, 100f, 1e-4f, 34.21f, 1234567f, 0.1f + 0.2f};
        for (float value : fixed) {
            assertEquals(SkillEffectText.pct(value), NumberText.format(value), "value " + value);
        }
        for (int i = 0; i < 10_000; i++) {
            float value = (random.nextFloat() - 0.5f) * (float) Math.pow(10, random.nextInt(6));
            if (random.nextBoolean()) {
                value = Math.round(value * 100f) / 100f;
            }
            assertEquals(SkillEffectText.pct(value), NumberText.format(value), "value " + value);
        }
    }
}

package exiledsector.skills.enemy;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnemyTreeRecordsTest {

    @Test
    void theRecordsMapIsCreatedInFleetMemoryOnceAndReused() {
        Map<String, Object> store = new HashMap<>();
        MemoryAPI memory = mock(MemoryAPI.class);
        when(memory.get(EnemyTreeRecords.MEMORY_KEY)).thenAnswer(invocation -> store.get(EnemyTreeRecords.MEMORY_KEY));
        doAnswer(invocation -> store.put(invocation.getArgument(0), invocation.getArgument(1))).when(memory).set(anyString(), any());

        Map<String, String> first = EnemyTreeRecords.of(memory);
        first.put("m1", EnemyTreeRecords.NOT_LEVELLED);

        assertSame(first, EnemyTreeRecords.of(memory));
        verify(memory).set(EnemyTreeRecords.MEMORY_KEY, first);
    }

    @Test
    void onlyEnemyTreeTagsCountAsLevelledRecords() {
        assertTrue(EnemyTreeRecords.isLevelled("exiledSector_enemyTree|bulwark|3|root,a"));
        assertFalse(EnemyTreeRecords.isLevelled(EnemyTreeRecords.NOT_LEVELLED));
        assertFalse(EnemyTreeRecords.isLevelled(null));
    }
}

package io.github.lilacbud.ownhashmap;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class OwnHashMapTest {
    @Test
    @SuppressWarnings("ThrowableResultIgnored")
    public void givenNullAsMap_whenCreatingOwnHashMap_thenThrow() {
        assertThrows(NullPointerException.class, () -> new OwnHashMap<String, String>(null));
    }
    
    @Test
    public void givenMap_whenCreatingOwnHashMap_thenCreateCopy() {
        Map<String, String> expectedMap = Map.of("Key1", "Value1", "Key2", "Value2", "Key3", "Value3");
        OwnHashMap<String, String> map = new OwnHashMap<>(expectedMap);
        assertEquals(expectedMap, map);
    }
    
    @Test
    public void givenThatMapIsEmpty_whenGettingValue_thenReturnNull() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        OwnHashMap<String, String> map = new OwnHashMap<>();
        String result = map.get("Key2");
        assertNull(result);
    }

    @Test
    public void givenThatEntryIsAbsent_whenGettingValue_thenReturnNull() {
        OwnHashMap<String, String> map = new OwnHashMap<>(Map.of("Key1", "Value1"));
        String result = map.get("Key2");
        assertNull(result);
    }
    
    @Test
    public void givenThatEntryIsPresent_whenGettingValue_thenReturnValue() {
        String expectedResult = "Value1";
        OwnHashMap<String, String> map = new OwnHashMap<>(Map.of("Key1", "Value1"));
        String result = map.get("Key1");
        assertEquals(expectedResult, result);
    }

    @Test
    public void givenThatEntryIsAbsent_whenPuttingValue_thenReturnNull() {
        Map<String, String> expectedMap = Map.of("Key1", "Value1");
        OwnHashMap<String, String> map = new OwnHashMap<>();
        String result = map.put("Key1", "Value1");
        assertNull(result);
        assertEquals(expectedMap, map);
    }
    
    @Test
    public void givenThatEntryIsPresent_whenPuttingValue_thenReturnOldValue() {
        Map<String, String> expectedMap = Map.of("Key1", "Value2");
        String expectedResult = "Value1";
        OwnHashMap<String, String> map = new OwnHashMap<>(Map.of("Key1", "Value1"));
        String result = map.put("Key1", "Value2");
        assertEquals(expectedResult, result);
        assertEquals(expectedMap, map);
    }

    @Test
    public void givenThatEntryIsPresent_whenPuttingValueIfAbsent_thenDoNotPutAndReturnOldValue() {
        Map<String, String> expectedMap = Map.of("Key1", "Value1");
        String expectedResult = "Value1";
        OwnHashMap<String, String> map = new OwnHashMap<>(Map.of("Key1", "Value1"));
        String result = map.putIfAbsent("Key1", "Value2");
        assertEquals(expectedResult, result);
        assertEquals(expectedMap, map);
    }

    @Test
    public void givenThatEntryIsAbsent_whenRemovingKey_thenReturnNull() {
        Map<String, String> expectedMap = Map.of("Key1", "Value1");
        OwnHashMap<String, String> map = new OwnHashMap<>(Map.of("Key1", "Value1"));
        String result = map.remove("Key2");
        assertNull(result);
        assertEquals(expectedMap, map);
    }
    
    @Test
    public void givenThatEntryIsPresent_whenRemovingKey_thenRemoveEntryAndReturnOldValue() {
        String expectedResult = "Value1";
        OwnHashMap<String, String> map = new OwnHashMap<>(Map.of("Key1", "Value1"));
        String result = map.remove("Key1");
        assertEquals(expectedResult, result);
        assertTrue(map.isEmpty());
    }

    @Test
    public void givenThatEntryValueDoesNotMatch_whenRemovingKeyAndValue_thenDoNotRemoveEntryAndReturnFalse() {
        Map<String, String> expectedMap = Map.of("Key1", "Value1");
        OwnHashMap<String, String> map = new OwnHashMap<>(Map.of("Key1", "Value1"));
        boolean result = map.remove("Key1", "Value2");
        assertFalse(result);
        assertEquals(expectedMap, map);
    }
    
    @Test void givenThatEntryValueMatches_whenRemovingKeyAndValue_thenRemoveEntryAndReturnTrue() {
        OwnHashMap<String, String> map = new OwnHashMap<>(Map.of("Key1", "Value1"));
        boolean result = map.remove("Key1", "Value1");
        assertTrue(result);
        assertTrue(map.isEmpty());
    }

    @Test
    public void givenThatMapIsNotEmpty_whenClearingMap_thenClearMap() {
        OwnHashMap<String, String> map = new OwnHashMap<>(Map.of("Key1", "Value1"));
        map.clear();
        assertTrue(map.isEmpty());
    }   
}

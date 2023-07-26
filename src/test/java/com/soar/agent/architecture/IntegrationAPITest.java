package com.soar.agent.architecture;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.soar.agent.architecture.api.controller.SemanticMemoryController;
import com.soar.agent.architecture.api.entity.SemanticMemoryEntity;
import com.soar.agent.architecture.api.service.SemanticMemoryService;

// @RunWith( SpringRunner.class ) //junit 4
@ExtendWith(SpringExtension.class) // junit 5, jupiter
// @WebMvcTest(SemanticMemoryController.class)
@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationAPITest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private SemanticMemoryService semanticMemoryService;

        @Test
        public void testGetMappingSuccessStatus() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", Set.of("red", "blue", "purple"));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);
                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk());
        }

        @Test
        public void testPostMappingSuccessStatus() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk());
        }      

        @Test
        public void testPostMappingStatusWithWrongarameterDataType() throws Exception {
                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{semanticMemoryEntity:testValue}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testPostMappingStatusWithoutParameterData() throws Exception {
                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testGetMappingReturnDataIsNotEmpty() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", Set.of("red", "blue", "purple"));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").isNotEmpty());
        }

        @Test
        public void testGetMappingReturnDataIsEmpty() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", new HashSet<>());

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").isEmpty());
        }

        @Test
        public void testGetMappingReturnDataExists() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", Set.of("red", "blue", "purple"));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists());
        }

        @Test
        public void testGetMappingReturnDataTypeIsMap() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", Set.of("red", "blue", "purple"));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isMap());
        }

        @Test
        public void testGetMappingReturnDataValueTypeIsArray() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", Set.of("red", "blue", "purple"));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color").isArray());
        }

        @Test
        public void testGetMappingReturnDataDoesNotExistsDifferentKeyNameAndValues() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("colors-detected", Set.of("red", "blue", "purple"));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").doesNotExist());
        }

        @Test
        public void testGetMappingReturnDataDoesNotExistsEmptyMap() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").doesNotExist());
        }

        @Test
        public void testGetMappingReturnDataSizeIsCorrect() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", Set.of("red", "blue", "purple"));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color").isNotEmpty())
                                .andExpect(jsonPath("$.color.length()").value(3));
        }

        @Test
        public void testGetMappingReturnDataFirstValue() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", new LinkedHashSet<>(List.of("red", "blue", "purple")));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color").isNotEmpty())
                                .andExpect(jsonPath("$.color[0]").value("red"));
        }

        @Test
        public void testGetMappingReturnDataFirstValueType() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", new LinkedHashSet<>(List.of("red", "blue", "purple")));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color").isNotEmpty())
                                .andExpect(jsonPath("$.color[0]").isString());
        }        

        @Test
        public void testGetMappingReturnDataSecondValue() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", new LinkedHashSet<>(List.of("red", "blue", "purple")));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color").isNotEmpty())
                                .andExpect(jsonPath("$.color[1]").value("blue"));
        }

        @Test
        public void testGetMappingReturnDataSecondValueType() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", new LinkedHashSet<>(List.of("red", "blue", "purple")));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color").isNotEmpty())
                                .andExpect(jsonPath("$.color[1]").isString());
        }        

        @Test
        public void testGetMappingReturnDataThirdValue() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", new LinkedHashSet<>(List.of("red", "blue", "purple")));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color").isNotEmpty())
                                .andExpect(jsonPath("$.color[2]").value("purple"));
        }

        @Test
        public void testGetMappingReturnDataThirdValueType() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", new LinkedHashSet<>(List.of("red", "blue", "purple")));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color").isNotEmpty())
                                .andExpect(jsonPath("$.color[2]").isString());
        }        

}

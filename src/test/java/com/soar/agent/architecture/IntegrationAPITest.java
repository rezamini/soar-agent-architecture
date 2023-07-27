package com.soar.agent.architecture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.oracle.truffle.tools.utils.json.JSONObject;
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
        public void testPostMappinReturnDataAttributesValueTypeIsArray() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color").isArray());
        }

        @Test
        public void testPostMappinReturnDataAttributesSizeOfThreeIsCorrect() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color.length()").value(3));
        }

        @Test
        public void testPostMappinReturnDataAttributesSizeOfTwoIsCorrect() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color.length()").value(2));
        }      
        
        @Test
        public void testPostMappinReturnDataAttributesSizeOfOneIsCorrect() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color.length()").value(1));
        }        

        @Test
        public void testPostMappinReturnDataAttributesSizeOfZeroIsCorrect() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", new ArrayList<>());

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color.length()").value(0));
        }          

        @Test
        public void testPostMappinReturnDataAttributesTypeIsMap() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes").isMap());
        }

        @Test
        public void testPostMappinReturnDataAttributesExists() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes").exists());
        }

        @Test
        public void testPostMappinReturnDataAttributesIsNotEmpty() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes").isNotEmpty());
        }

        @Test
        public void testPostMappinReturnDataAttributesIsEmpty() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                // tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes").isEmpty());
        }      
        
        
        @Test
        public void testPostMappinReturnDataObjectExists() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                // tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").exists());
        }          

        @Test
        public void testPostMappinReturnDataObjectIsMap() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                // tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isMap());
        }         

        @Test
        public void testPostMappinReturnDataAttributesFirstValue() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes-parent-name");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color[0]").value("red"));
        }

        @Test
        public void testPostMappinReturnDataAttributesFirstValueType() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes-parent-name");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color[0]").isString());
        }

        @Test
        public void testPostMappinReturnDataAttributesSecondValue() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes-parent-name");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color[1]").value("blue"));
        }

        @Test
        public void testPostMappinReturnDataAttributesSecondValueType() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes-parent-name");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color[1]").isString());
        }

        @Test
        public void testPostMappinReturnDataAttributesThirdValue() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes-parent-name");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color[2]").value("purple"));
        }

        @Test
        public void testPostMappinReturnDataAttributesThirdValueType() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.attributes.color[2]").isString());
        }

        @Test
        public void testPostMappinReturnDataObjectParentNameField() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes-parent-name");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("attributes-parent-name"));
        }

        @Test
        public void testPostMappinReturnDataObjectParentNameFieldExists() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes-parent-name");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").exists());
        }

        @Test
        public void testPostMappinReturnDataObjectParentNameFieldIsNotEmpty() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                tempEntity.setParentName("attributes-parent-name");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").isNotEmpty());
        }

        @Test
        public void testPostMappinReturnDataObjectParentNameFieldIsEmpty() throws Exception {
                Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
                tempMap.put("color", List.of("red", "blue", "purple"));

                SemanticMemoryEntity tempEntity = new SemanticMemoryEntity();
                tempEntity.setAttributes(tempMap);
                // tempEntity.setParentName("attributes-parent-name");

                when(semanticMemoryService.saveSemanticMemoryAttributes(any(SemanticMemoryEntity.class)))
                                .thenReturn(tempEntity);

                mockMvc.perform(post("/smemAttributes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"semanticMemoryEntity\" : \"" + tempEntity + "\"}"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").isEmpty());
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
        public void testGetMappingReturnDataDoesNotExistsNoMap() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").doesNotExist());
        }

        @Test
        public void testGetMappingReturnDataofThreeIsCorrect() throws Exception {
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
        public void testGetMappingReturnDataSizeOfTwoIsCorrect() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", Set.of("red", "blue"));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color").isNotEmpty())
                                .andExpect(jsonPath("$.color.length()").value(2));
        }        

        @Test
        public void testGetMappingReturnDataSizeOfOneIsCorrect() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", Set.of("red"));

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color").isNotEmpty())
                                .andExpect(jsonPath("$.color.length()").value(1));
        }     
        
        @Test
        public void testGetMappingReturnDataSizeOfZeroIsCorrect() throws Exception {
                Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
                tempMap.put("color", new HashSet<>());

                when(semanticMemoryService.getSemanticMemoryAttributes())
                                .thenReturn(tempMap);

                mockMvc.perform(get("/smemAttributes"))
                                // .andDo(print()) // to see the result in the console
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.color").exists())
                                .andExpect(jsonPath("$.color.length()").value(0));
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

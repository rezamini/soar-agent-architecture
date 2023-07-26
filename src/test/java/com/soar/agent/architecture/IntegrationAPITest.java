package com.soar.agent.architecture;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.HashMap;
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



}

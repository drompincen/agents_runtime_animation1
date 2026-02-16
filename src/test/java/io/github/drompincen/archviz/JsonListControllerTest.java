package io.github.drompincen.archviz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JsonListController.class)
class JsonListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listJsonFiles_returnsHtml() throws Exception {
        mockMvc.perform(get("/json/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    void listJsonFiles_containsLinks() throws Exception {
        mockMvc.perform(get("/json/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<a href=")));
    }

    @Test
    void listJsonFiles_containsJsonFilenames() throws Exception {
        mockMvc.perform(get("/json/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(".json")));
    }
}

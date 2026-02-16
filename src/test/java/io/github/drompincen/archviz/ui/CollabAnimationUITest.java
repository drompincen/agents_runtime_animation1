package io.github.drompincen.archviz.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CollabAnimationUITest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private JavascriptExecutor js;

    @BeforeEach
    void setUp() {
        HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver(true);
        htmlUnitDriver.getWebClient().getOptions().setThrowExceptionOnScriptError(false);
        driver = htmlUnitDriver;
        js = (JavascriptExecutor) driver;
        driver.get("http://localhost:" + port + "/collab-animation.html");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private boolean hasClass(WebElement el, String className) {
        String classes = el.getAttribute("class");
        return classes != null && classes.contains(className);
    }

    @Test
    void pageLoads_titleAndHeaderVisible() {
        assertEquals("DROM: Architecture Viz", driver.getTitle());

        WebElement brand = driver.findElement(By.cssSelector(".brand"));
        assertTrue(brand.getText().toUpperCase().contains("DROM"));
    }

    @Test
    void editorPanel_hasDefaultJson() {
        WebElement textarea = driver.findElement(By.id("json-input"));
        String value = textarea.getAttribute("value");
        assertTrue(value.contains("Simple API Flow"),
                "Editor should contain default demo JSON");
        assertTrue(value.contains("\"nodes\""),
                "Editor JSON should have nodes array");
    }

    @Test
    void renderButton_createsNodes() {
        WebElement container = driver.findElement(By.id("nodes-container"));
        List<WebElement> nodes = container.findElements(By.cssSelector(".node"));
        assertTrue(nodes.size() >= 3,
                "Default diagram should render at least 3 nodes, found " + nodes.size());
    }

    @Test
    void renderButton_createsConnections() {
        WebElement svgLayer = driver.findElement(By.id("connections-layer"));
        List<WebElement> paths = svgLayer.findElements(By.cssSelector("path.connector"));
        assertTrue(paths.size() >= 2,
                "Default diagram should have at least 2 connections, found " + paths.size());
    }

    @Test
    void invalidJson_showsError() {
        WebElement textarea = driver.findElement(By.id("json-input"));
        textarea.clear();
        textarea.sendKeys("{ invalid json !!!");

        WebElement updateBtn = driver.findElement(By.id("btn-update"));
        updateBtn.click();

        WebElement errorMsg = driver.findElement(By.id("error-msg"));
        assertTrue(hasClass(errorMsg, "visible"),
                "Error message should become visible for invalid JSON");
    }

    @Test
    void themeToggle_switchesToLight() {
        WebElement checkbox = driver.findElement(By.id("chk-light-mode"));

        String bodyClassBefore = (String) js.executeScript("return document.body.className;");
        assertFalse(bodyClassBefore.contains("light-theme"),
                "Body should not have light-theme initially");

        js.executeScript("arguments[0].click();", checkbox);

        String bodyClassAfter = (String) js.executeScript("return document.body.className;");
        assertTrue(bodyClassAfter.contains("light-theme"),
                "Body should have light-theme after toggling");
    }

    @Test
    void editorToggle_togglesSidebar() {
        WebElement checkbox = driver.findElement(By.id("chk-show-editor"));
        WebElement sidebar = driver.findElement(By.id("left-sidebar"));

        // Sidebar starts hidden in HTML; show-editor checkbox is checked by default
        // but the sidebar CSS class is "hidden" initially
        boolean initiallyHidden = hasClass(sidebar, "hidden");

        // Click to toggle
        js.executeScript("arguments[0].click();", checkbox);
        boolean afterFirstClick = hasClass(sidebar, "hidden");

        // State should change after clicking
        assertNotEquals(initiallyHidden, afterFirstClick,
                "Editor visibility should toggle after clicking checkbox");
    }

    @Test
    void notesToggle_hidesNotes() {
        WebElement checkbox = driver.findElement(By.id("chk-show-notes"));
        WebElement notebook = driver.findElement(By.id("notebook-widget"));

        js.executeScript("arguments[0].click();", checkbox);

        assertTrue(hasClass(notebook, "hidden"),
                "Notebook should be hidden after unchecking toggle");
    }

    @Test
    void optionsDropdown_opensOnClick() {
        WebElement optionsBtn = driver.findElement(By.id("btn-options"));
        WebElement dropdown = driver.findElement(By.id("options-dropdown"));

        assertFalse(hasClass(dropdown, "open"),
                "Dropdown should be closed initially");

        optionsBtn.click();

        assertTrue(hasClass(dropdown, "open"),
                "Dropdown should open after clicking options button");
    }

    @Test
    void diagramSelector_hasDefaultOption() {
        WebElement selector = driver.findElement(By.id("json-selector"));
        List<WebElement> options = selector.findElements(By.tagName("option"));
        assertTrue(options.size() >= 1,
                "Diagram selector should have at least the default option");

        assertEquals("__default__", options.get(0).getAttribute("value"));
    }

    @Test
    void playButton_exists() {
        WebElement playBtn = driver.findElement(By.id("btn-play"));
        assertNotNull(playBtn);
        assertTrue(playBtn.isEnabled(), "Play button should be enabled");
    }

    @Test
    void stripJsonComments_worksViaRender() {
        WebElement textarea = driver.findElement(By.id("json-input"));

        String jsonWithComments = "{\n" +
                "    // This is a comment\n" +
                "    \"title\": \"Comment Test\",\n" +
                "    \"nodes\": [\n" +
                "        { \"id\": \"a\", \"type\": \"service\", \"label\": \"A\", \"x\": 100, \"y\": 100 }\n" +
                "    ],\n" +
                "    \"connections\": []\n" +
                "}";

        js.executeScript("arguments[0].value = arguments[1];", textarea, jsonWithComments);

        WebElement updateBtn = driver.findElement(By.id("btn-update"));
        updateBtn.click();

        WebElement errorMsg = driver.findElement(By.id("error-msg"));
        assertFalse(hasClass(errorMsg, "visible"),
                "Comment-stripped JSON should parse without error");

        WebElement title = driver.findElement(By.id("phase-display"));
        assertEquals("Comment Test", title.getText());
    }
}

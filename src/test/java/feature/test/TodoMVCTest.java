package feature.test;

import com.codeborne.selenide.*;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.allure.annotations.Step;

import java.io.File;
import java.io.IOException;

import static com.codeborne.selenide.CollectionCondition.empty;
import static com.codeborne.selenide.CollectionCondition.exactTexts;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

/**
 * Created by barocko on 7/25/2016.
 */
public class TodoMVCTest {

    @Before
    public void clearScreenshotList(){
        Screenshots.screenshots.getScreenshots().clear();
    }

    @After
    public void tearDown() throws IOException {
        File lastSelenideScreenshot = Screenshots.takeScreenShotAsFile();
        if (lastSelenideScreenshot != null) {
            screenshot(Files.toByteArray(lastSelenideScreenshot));
        }
    }

    @Attachment(type = "image/png")
    public static byte[] screenshot(byte[] dataForScreenshot) {
        return dataForScreenshot;
    }

    @Before
    public void openPage() {
        open("https://todomvc4tasj.herokuapp.com/");
    }

    @After
    public void clearData() {
        executeJavaScript("localStorage.clear()");
    }

    @Test
    public void testTaskLifeCycle() {

        add("1");
        toggle("1");
        assertTasksAre("1");

        filterActive();
        assertNoVisibleTasks();
        add("2");
        assertVisibleTasksAre("2");
        toggleAll();
        assertNoVisibleTasks();

        filterCompleted();
        assertVisibleTasksAre("1", "2");
        //reopen
        toggle("2");
        assertVisibleTasksAre("1");
        clearCompleted();
        assertNoVisibleTasks();

        filterAll();
        assertTasksAre("2");
        assertItemsLeft(1);
    }

    @Test
    public void testEditAtAll() {
        //precondition-added tasks
        add("1", "2");

        edit("1", "1 edited");

        assertTasksAre("1 edited", "2");
        assertItemsLeft(2);
    }

    @Test
    public void testDeleteAtActive() {
        //precondition-added tasks
        add("1", "2");
        filterActive();

        delete("1");

        assertTasksAre("2");
        assertItemsLeft(1);
    }

    @Test
    public void testCancelEditAtCompleted() {
        //precondition-completed tasks
        add("1", "2");
        toggleAll();
        filterCompleted();

        cancelEdit("2", "to be canceled");

        assertTasksAre("1", "2");
        assertItemsLeft(0);
    }

    ElementsCollection tasks = $$("#todo-list li");

    @Step
    private void clearCompleted() {
        $("#clear-completed").click();
    }

    @Step
    private void add(String... taskTexts) {
        for (String text : taskTexts) {
            $("#new-todo").setValue(text).pressEnter();
        }
    }

    @Step
    private void delete(String taskText) {
        tasks.find(exactText(taskText)).hover().$(".destroy").click();
    }

    @Step
    private void toggle(String taskText) {
        tasks.find(exactText(taskText)).$(".toggle").click();
    }

    @Step
    private void toggleAll() {
        $("#toggle-all").click();
    }

    @Step
    private void assertTasksAre(String... taskTexts) {
        tasks.shouldHave(exactTexts(taskTexts));
    }

    @Step
    private void assertNoTasks() {
        tasks.shouldBe(empty);
    }

    @Step
    private SelenideElement startEdit(String oldTaskText, String newTaskText) {
        tasks.find(exactText(oldTaskText)).doubleClick();
        return tasks.find(cssClass("editing")).find(".edit").setValue(newTaskText);
    }

    @Step
    private void cancelEdit(String oldTaskText, String newTaskText) {
        startEdit(oldTaskText, newTaskText).pressEscape();
    }

    @Step
    private void edit(String oldTaskText, String newTaskText) {
        startEdit(oldTaskText, newTaskText).pressEnter();
    }

    @Step
    private void filterActive() {
        $(By.linkText("Active")).click();
    }

    @Step
    private void filterCompleted() {
        $(By.linkText("Completed")).click();
    }

    @Step
    private void filterAll() {
        $(By.linkText("All")).click();
    }

    @Step
    private void assertItemsLeft(Integer count) {
        $("#todo-count>strong").shouldHave(exactText((count.toString())));
    }

    @Step
    private void assertNoVisibleTasks() {
        tasks.filter(visible).shouldBe(empty);
    }

    @Step
    private void assertVisibleTasksAre(String... taskTexts) {
        tasks.filter(visible).shouldHave(exactTexts(taskTexts));
    }
}




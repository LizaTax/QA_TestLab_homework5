package myprojects.automation.assignment5;


import myprojects.automation.assignment5.model.ProductData;
import myprojects.automation.assignment5.utils.logging.CustomReporter;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.util.List;
import java.util.Random;

/**
 * Contains main script actions that may be used in scripts.
 */
public class GeneralActions {
    private WebDriver driver;
    private WebDriverWait wait;

    public GeneralActions(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, 15);
    }

    public void verifyMobileVersion() {
        verifyElementPresent(By.cssSelector(".mobile #menu-icon"));
    }

    public void verifyDesktopVersion() {
        verifyElementPresent(By.id("_desktop_currency_selector"));
    }

    public void openRandomProduct() {
        int qty = driver.findElements(By.cssSelector(".product-title")).size();
        Random rand = new Random();
        int randomNum = 1 + rand.nextInt((qty - 1) + 1);
        clickOnElement(By.cssSelector(String.format(".products > .product-miniature:nth-child(%d) .product-title", randomNum)));
    }

    /**
     * Extracts product information from opened product details page.
     *
     * @return
     */
    public ProductData getOpenedProductInfo() {
        CustomReporter.logAction("Get information about currently opened product");
        String name = toTitleCase(getTextFromElement(By.cssSelector(".h1[itemprop=\"name\"]")).toLowerCase());
        String price = getTextFromElement(By.cssSelector(".current-price > span[itemprop=\"price\"]")).replace(",", ".").replace(" ₴", "");
        scrollToElement(By.cssSelector(".nav-tabs > li:nth-child(2)"));
        clickOnElement(By.cssSelector(".nav-tabs > li:nth-child(2)"));
        verifyElementAppears(By.cssSelector(".product-quantities > span"));
        int qty = Integer.valueOf(getTextFromElement(By.cssSelector(".product-quantities > span")).replace(" Товары", ""));
        ProductData product = new ProductData(name, qty, Float.valueOf(price));
        return product;
    }

    public void clickOnShowAllLink() {
        scrollToElement(By.cssSelector(".all-product-link"));
        clickOnElement(By.cssSelector(".all-product-link"));
    }

    public void addToCartAndGoToBasket() {
        clickOnElement(By.cssSelector(".add-to-cart"));
        verifyElementAppears(By.cssSelector("a.btn-primary"));
        clickOnElement(By.cssSelector("a.btn-primary"));
    }

    public void verifyAddedProduct(ProductData productData) {
        Assert.assertEquals(getTextFromElement(By.cssSelector(".js-subtotal")), "1 шт.");
        verifyElementWithTextPresent(By.cssSelector(".product-line-info > a"), productData.getName());
        verifyElementWithTextPresent(By.className("product-price"), String.format("%.2f ₴", productData.getPrice()));
    }

    public void clickOnMakeOrderButton() {
        clickOnElement(By.cssSelector(".checkout a.btn-primary"));
    }

    public void fillRequiredInformation() {
        setEditText(By.cssSelector("#checkout-guest-form input[name=\"firstname\"]"), "Name " + randomString());
        setEditText(By.cssSelector("#checkout-guest-form input[name=\"lastname\"]"), "Surname " + randomString());
        setEditText(By.cssSelector("#checkout-guest-form input[name=\"email\"]"), String.format("webinar.test+%s@gmail.com ", randomString()));
        clickOnElement(By.cssSelector("#checkout-guest-form button.continue"));
        setEditText(By.cssSelector("#delivery-address input[name=\"address1\"]"), "Гоголя, 5");
        setEditText(By.cssSelector("#delivery-address input[name=\"postcode\"]"), "12345");
        setEditText(By.cssSelector("#delivery-address input[name=\"city\"]"), "Киев");
        clickOnElement(By.cssSelector("#delivery-address button.continue"));
        clickOnElement(By.cssSelector("#js-delivery button.continue"));
        clickOnElement(By.cssSelector("label[for=\"payment-option-1\"]"));
        setCheckBoxValue(By.id("conditions_to_approve[terms-and-conditions]"), true);
        clickOnElement(By.cssSelector("#payment-confirmation .btn-primary"));
    }

    public void checkOrderDetails(ProductData productData) {
        verifyElementWithTextPresent(By.cssSelector("#content-hook_order_confirmation .card-title"), "ВАШ ЗАКАЗ ПОДТВЕРЖДЁН");
        verifyElementWithTextPresent(By.cssSelector(".order-confirmation-table .details > span"), productData.getName());
        verifyElementWithTextPresent(By.cssSelector(".order-confirmation-table .qty > div > .col-xs-2"), "1");
        verifyElementWithTextPresent(By.cssSelector(".order-confirmation-table .qty > div > .col-xs-5"), String.format("%.2f ₴", productData.getPrice()));
    }

    public void checkUpdateInStock(ProductData productData) {
        clickOnElement(By.linkText(productData.getName()));
        clickOnElement(By.cssSelector(".nav-tabs > li:nth-child(2)"));
        verifyElementAppears(By.cssSelector(".product-quantities > span"));
        verifyElementWithTextPresent(By.cssSelector(".product-quantities > span"), String.format("%s Товары", (productData.getQty() - 1)));
    }

    public String randomString() {
        String chars = "qwertyuiopasdfghjklzxcvbnm";
        Random rand = new Random();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            buf.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return buf.toString();
    }

    public static String toTitleCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();
        for (String anArr : arr) {
            sb.append(Character.toUpperCase(anArr.charAt(0))).append(anArr.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }


    /**
     * Waits until page loader disappears from the page
     */
    public void waitForContentLoad() {
        wait.until((ExpectedCondition<Boolean>) wd ->
                ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
    }

    public void openUrl(String url) {
        driver.get(url);
    }

    private void clickOnElement(By by) {
        WebElement e = wait.until(ExpectedConditions.elementToBeClickable(by));
        if (e == null) {
            throw new ElementNotInteractableException(String.format("Element %s can not be clicked", by));
        }
        e.click();
    }

    private void scrollToElement(By by) {
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView(true);", driver.findElement(by));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setEditText(By by, String text) {
        WebElement element = driver.findElement(by);
        if (!element.getAttribute("value").isEmpty()) {
            element.clear();
        }
        element.sendKeys(text);
    }

    private void verifyElementAppears(By by) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private void verifyElementPresent(By by) {
        driver.findElement(by);
    }

    private void verifyElementWithTextPresent(By by, String text) {
        WebElement element = driver.findElement(by);
        if (!element.getText().contains(text)) {
            element.findElement(By.xpath(String.format(".//*[contains(., '%s')]", text)));
        }
        verifyElementIsVisible(by);
    }

    private String getTextFromElement(By by) {
        return driver.findElement(by).getText();
    }

    private void verifyElementIsVisible(By by) {
        if (!driver.findElement(by).isDisplayed()) {
            throw new NotFoundException("Error: element " + by + " is not visible on the page!");
        }
    }

    protected void setCheckBoxValue(By by, boolean selected) {
        while (selected != driver.findElement(by).isSelected()) {
            driver.findElement(by).click();
        }
    }
}

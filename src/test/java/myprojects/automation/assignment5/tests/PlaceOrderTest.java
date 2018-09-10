package myprojects.automation.assignment5.tests;

import myprojects.automation.assignment5.BaseTest;
import myprojects.automation.assignment5.model.ProductData;
import org.testng.annotations.Test;

public class PlaceOrderTest extends BaseTest {

    private static final String baseUrl = "http://prestashop-automation.qatestlab.com.ua";

    @Test
    public void checkSiteVersion() {
        actions.openUrl(baseUrl + "/ru/");
        if (isMobileTesting) {
            actions.verifyMobileVersion();
        } else {
            actions.verifyDesktopVersion();
        }
    }

    @Test(dependsOnMethods = "checkSiteVersion")
    public void createNewOrder() {
        // open random product
        actions.clickOnShowAllLink();
        actions.waitForContentLoad();
        actions.openRandomProduct();
        // save product parameters
        ProductData product = actions.getOpenedProductInfo();
        // add product to Cart and validate product information in the Cart
        actions.addToCartAndGoToBasket();
        actions.verifyAddedProduct(product);
        // proceed to order creation, fill required information
        actions.clickOnMakeOrderButton();
        actions.fillRequiredInformation();
        // place new order and validate order summary
        actions.checkOrderDetails(product);
        // check updated In Stock value
        actions.openUrl(baseUrl + "/ru/");
        actions.clickOnShowAllLink();
        actions.checkUpdateInStock(product);
    }
}

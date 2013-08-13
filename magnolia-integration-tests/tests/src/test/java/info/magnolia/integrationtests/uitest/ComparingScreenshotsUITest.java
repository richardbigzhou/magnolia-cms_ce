/**
 * This file Copyright (c) 2013 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.integrationtests.uitest;

import static org.junit.Assert.fail;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.SeleniumException;

/**
 * Test for page layout based on comparing screenshots.
 * If the layout will change intentionally, download screenshots from {@link http://hudson.magnolia-cms.com/job/X/ws/magnolia-ee-integration-tests/tests/currentScreenshots},
 * check them visually and copy them to src/test/resources/screenshots/
 */
public class ComparingScreenshotsUITest extends AbstractMagnoliaUITest {

    private final String[] supportedUserAgents = new String[] { "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:11.0) Gecko/20100101 Firefox/11.0" };
    private final int tolerance = 5; // % of wrong pixels tolerance

    private static final boolean deleteScreenshotsAfterTests = true; // set to false to check the screenshots visually in 'currentScreenshots' directory

    /**
     * Set to true if you want to compare converted (color space, crop) original and current screenshots.
     * They will be saved in currentScreenshots directory, original named as *Original.png next to current screenshots.
     */
    private final boolean saveConvertedScreenshots = false;

    private static final Logger log = LoggerFactory.getLogger(ComparingScreenshotsUITest.class);
    private final String tmpPath = System.getProperty("user.dir") + "/currentScreenshots/";
    private Instance instance;
    private Map<String, Float> different;
    private float averageError = 0;
    private Float currentPercentage;

    @Test
    public void testComparePagesOnAuthor() throws Exception {
        if (!this.isUserAgentSupported()) {
            return;
        }

        // GIVEN
        different = new HashMap<String, Float>();
        instance = Instance.AUTHOR;
        averageError = 0;
        List website = readConfiguration("website");

        // WHEN
        this.comparePages(website);

        // THEN
        log.info("{} average error: {}%\n", instance, averageError / website.size());
        if (different.size() != 0) {
            fail(different.size() + " screenshots from " + instance + " differ from original:" + this.different + ". Average pixel error is: " + averageError / website.size() + "%.");
        }
    }

    @Test
    public void testComparePagesOnPublic() throws Exception {
        if (!this.isUserAgentSupported()) {
            return;
        }

        // GIVEN
        different = new HashMap<String, Float>();
        instance = Instance.PUBLIC;
        averageError = 0;
        List website = readConfiguration("website");

        // WHEN
        this.comparePages(website);

        // THEN
        log.info("{} average error: {}%\n", instance, averageError / website.size());
        if (different.size() != 0) {
            fail(different.size() + " screenshots from " + instance + " differ from original:" + this.different + ". Average pixel error is: " + averageError / website.size() + "%.");
        }
    }

    private List readConfiguration(String configName) throws IOException {
        return FileUtils.readLines(new File("src/test/resources/screenshots/listsOfURLsToCheck/" + instance.getContextPath() + configName));
    }

    private void comparePages(List<String> url) throws Exception {

        for (String page : url) {
            if (StringUtils.isEmpty(page)) { // allow empty lines on configuration files (src/test/resources/listsOfURLsToCheck)
                continue;
            }
            try {
                takePageScreenShot(page);
                compareScreenshots(instance.getContextPath() + "/" + page);
            } catch (SeleniumException e) {
                log.error("Error when trying to compare screenshots of page: {}.", page, e);
            }
        }
    }

    private void takePageScreenShot(String path) throws Exception {
        final String url = instance.getURL(path);
        log.info("Opening url: {}.", url);
        driver.navigate().to(url);
        this.takeScreenshot(tmpPath + instance.getContextPath() + path);
    }

    @Override
    protected void takeScreenshot(String fullFileName) {
        if (driver instanceof TakesScreenshot) {
            TakesScreenshot screenshotter = (TakesScreenshot) driver;
            File file = screenshotter.getScreenshotAs(OutputType.FILE);
            try {
                // cut if required - fileNames lengths are normally restricted
                fullFileName = fullFileName.length() > MAX_FILE_NAME_LENGHT_WITHOUT_EXTENSION ? fullFileName.substring(0, MAX_FILE_NAME_LENGHT_WITHOUT_EXTENSION) : fullFileName;
                FileUtils.copyFile(file, new File(fullFileName + ".png"));
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void compareScreenshots(String pageName) {
        pageName = StringUtils.substringBefore(pageName, "?");
        final String originalFilePath = "src/test/resources/screenshots/" + StringUtils.substringAfter(pageName, "/") + ".png";
        final String currentFilePath = "currentScreenshots/" + pageName + ".png";

        File original = new File(originalFilePath);
        File current = new File(currentFilePath);

        // don't delete screenshots if there was a difference between original and current
        if (!compareImages(original, current)) {
            log.error("{}% pixels of page {} differs from original.", currentPercentage, pageName);
            this.different.put(pageName, currentPercentage);
        } else if (ComparingScreenshotsUITest.deleteScreenshotsAfterTests) {
            current.delete();
        }
    }

    private boolean compareImages(File original, final File current) {
        try {
            BufferedImage originalImage = ImageIO.read(original);
            BufferedImage currentImage = ImageIO.read(current);

            // convert to B&W, author instance - page editor has problem with color shades (especially gray empty areas)
            originalImage = changeImageType(originalImage, BufferedImage.TYPE_BYTE_BINARY);
            currentImage = changeImageType(currentImage, BufferedImage.TYPE_BYTE_BINARY);

            if (this.saveConvertedScreenshots) {
                // even if this is not necessary for comparing screenshots automatically, for visual check is good to have the same size for both original and current screenshots and just slide between them in image viewer
                int width = originalImage.getWidth() < currentImage.getWidth() ? originalImage.getWidth() : currentImage.getWidth();
                int height = originalImage.getHeight() < currentImage.getHeight() ? originalImage.getHeight() : currentImage.getHeight();

                log.info("Resizing from: sample {}x{}, current {}x{} to {}x{}", new Object[] {
                        originalImage.getWidth(), originalImage.getHeight(), currentImage.getWidth(), currentImage.getHeight(), width, height
                });
                originalImage = originalImage.getSubimage(0, 0, width, height);
                currentImage = currentImage.getSubimage(0, 0, width, height);

                // save into files
                ImageIO.write(originalImage, "png", new File(StringUtils.substringBefore(current.getAbsolutePath(), ".png") + "Original.png"));
                ImageIO.write(currentImage, "png", current);
            }

            return equalsPictures(originalImage, currentImage, tolerance);
        } catch (RasterFormatException e) {
            log.error("Can't resize original and current scrrenshot to fit the size: {}, {}", new Object[] { original, current, e });
            return false;
        } catch (IOException e) {
            log.error("Can't read input files: {}, {}", new Object[] { original, current, e });
            return false;
        }
    }

    private BufferedImage changeImageType(BufferedImage currentImage, int imageType) throws IOException {
        BufferedImage image = new BufferedImage(currentImage.getWidth(), currentImage.getHeight(), imageType);
        Graphics g = image.getGraphics();
        g.drawImage(currentImage, 0, 0, null);
        g.dispose();
        return image;
    }

    private boolean equalsPictures(BufferedImage originalImage, BufferedImage currentImage, int percentage) {

        if (originalImage.getWidth() != currentImage.getWidth() || originalImage.getHeight() != currentImage.getHeight()) {
            log.warn("Screenshots have got different size: sample: {}x{}, current: {}x{}", new Object[] {
                    originalImage.getWidth(), originalImage.getHeight(), currentImage.getWidth(), currentImage.getHeight()
            });
        }
        int wrongPixels = 0;
        int width = originalImage.getWidth() < currentImage.getWidth() ? originalImage.getWidth() : currentImage.getWidth();
        int height = originalImage.getHeight() < currentImage.getHeight() ? originalImage.getHeight() : currentImage.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (originalImage.getRGB(x, y) != currentImage.getRGB(x, y)) {
                    wrongPixels++;
                }
            }
        }

        currentPercentage = (float) wrongPixels / (originalImage.getWidth() * originalImage.getHeight()) * 100;
        averageError += currentPercentage;

        log.info("{} wrong pixels from total of {} pixels. This is {}%.", new Object[] {
                wrongPixels,
                originalImage.getWidth() * originalImage.getHeight(),
                currentPercentage
        });

        if (currentPercentage > percentage) {
            return false;
        }
        return true;
    }

    @AfterClass
    public static void afterClassTearDown() throws Exception {
        /**
         * uncomment this to take new screenshots of pages if the layout was intentionally changed
         */
        // ComparingScreenshotsUITest.takeCurrentScreenshotAsOriginalSamples();
    }

    /**
     * Use this method when adding new dialogs to copy current screenshots of dialogs to original samples directory (src/test/resources/screenshots/X/.magnolia/dialogs...)
     * Check them visually before push!
     * We need to filter out converted original images in case of {@link #saveConvertedScreenshots} is true.
     * You can use this only for dialogs, appearance of pages is OS dependent, so you have to download sample screenshots from {@link http://hudson.magnolia-cms.com/view/X/job/magnolia-bundle_4.5.x-with-selenium_profile/ws/magnolia-integration-tests/tests/}.
     */
    private static void takeCurrentScreenshotAsOriginalSamples() throws IOException {

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !StringUtils.endsWith(pathname.getName(), "Original.png");
            }
        };

        String[] directories = new String[] { "magnoliaTest", "magnoliaTestPublic" };
        for (final String directory : directories) {
            FileUtils.copyDirectory(new File("currentScreenshots/" + directory), new File("src/test/resources/screenshots/" + directory), filter, false);
        }
    }

    private boolean isUserAgentSupported() {
        String userAgent = (String) ((JavascriptExecutor) driver).executeScript("return navigator.userAgent;");

        for (String browserVersion : this.supportedUserAgents) {
            if (userAgent.contains(browserVersion)) {
                return true;
            }
        }
        log.warn("Skipping test: yout user agent is {}, but this test currently runs only on {}\n", userAgent, supportedUserAgents);
        return false;
    }
}

package de.ids_mannheim.korap.config;

import de.ids_mannheim.korap.exceptions.KustvaktException;
import de.ids_mannheim.korap.utils.ServiceVersion;
import de.ids_mannheim.korap.utils.TimeUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author hanl
 * @date 02/09/2015
 */
public class ConfigTest {

    @After
    public void close() {
        BeanConfiguration.closeApplication();
    }


    @Test
    public void create() {
        BeanConfiguration.loadClasspathContext("test-config.xml");
    }

    @Test
    public void testServiceVersion() {
        String v = ServiceVersion.getAPIVersion();
        Assert.assertNotEquals("wrong version", "UNKNOWN", v);
    }

    @Test
    public void testPropertiesOverride() {
        BeanConfiguration.loadClasspathContext();

        Assert.assertEquals("token layer does not match", "opennlp",
                BeanConfiguration.getBeans().getConfiguration()
                        .getDefault_token());
        Assert.assertEquals("token expiration does not match",
                TimeUtils.convertTimeToSeconds("150D"),
                BeanConfiguration.getBeans().getConfiguration()
                        .getLongTokenTTL());

        BeanConfiguration.getBeans().getConfiguration().setPropertiesAsStream(
                ConfigTest.class.getClassLoader()
                        .getResourceAsStream("kustvakt.conf"));

        Assert.assertEquals("token layer does not match", "tt",
                BeanConfiguration.getBeans().getConfiguration()
                        .getDefault_token());
        Assert.assertEquals("token expiration does not match",
                TimeUtils.convertTimeToSeconds("230D"),
                BeanConfiguration.getBeans().getConfiguration()
                        .getLongTokenTTL());
    }

    @Test(expected = KustvaktException.class)
    public void testBeanOverrideInjection() throws KustvaktException {
        BeanConfiguration.loadClasspathContext("test-config.xml");

        BeanConfiguration.getBeans().getConfiguration().setPropertiesAsStream(
                ConfigTest.class.getClassLoader()
                        .getResourceAsStream("kustvakt.conf"));

        String v = "testmail@ids-mannheim.de";
        BeanConfiguration.getBeans().getEncryption().validateEmail(v);
    }
}


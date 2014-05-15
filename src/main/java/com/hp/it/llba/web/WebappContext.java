package com.hp.it.llba.web;

import com.hp.it.llba.beans.Configuration;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
@Component("WebappContext")
public class WebappContext {
    @Autowired
    private Configuration configuration;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WebappContext.class.getName());

	public static final String PARAMETER_FILEUPLOAD_FILE_SIZE_MAX = "fileupload.fileSizeMax";

	private static final String KEY = WebappContext.class.getName();

	private ServletFileUpload fileUpload = null;

	private static OfficeManager officeManager = null;
	private static OfficeDocumentConverter documentConverter = null;

    public WebappContext() {

    }

	public void WebappContextPostConstr(ServletContext servletContext) {
        int ports[];

        String officeHomeParam =  configuration.getOfficeHomeParam();
        String portNumbers = configuration.getPortNumbers();
        String taskQueueTimeout = configuration.getTaskQueueTimeout();
        String taskExecutionTimeout = configuration.getTaskExecutionTimeout();
        String maxTasksPerProcess = configuration.getMaxTasksPerProcess();
        String retryTimeout = configuration.getRetryTimeout();

        DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
        if (portNumbers != null) {
            try {
                String[] portsList = portNumbers.split(",");
                ports = new int[portsList.length];

                for (int i = 0; i < portsList.length; i++) {
                    ports[i] = Integer.parseInt(portsList[i].trim());
                }
                configuration.setPortNumbers(ports);
            } catch (NumberFormatException nfe) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Wrong configuration ==> Use default portNumbers value of DefaultOfficeManagerConfiguration");
                }
            }
        }
        // in case of not setting office home, JODConverter will use system default
        // office home by using OfficeUtils.getDefaultOfficeHome();
        if (officeHomeParam != null && officeHomeParam.trim().length() != 0) {
            try {
                configuration.setOfficeHome(officeHomeParam);
            } catch (IllegalArgumentException iae) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Wrong configuration ==> Use default officeHome value of DefaultOfficeManagerConfiguration");
                }
            }
        }

        if (taskQueueTimeout != null) {
            try {
                configuration.setTaskQueueTimeout(Long.parseLong(taskQueueTimeout));
            } catch (NumberFormatException nfe) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Wrong configuration ==> Use default taskQueueTimeout value of DefaultOfficeManagerConfiguration");
                }
            }
        }

        if (taskExecutionTimeout != null) {
            try {
                configuration.setTaskExecutionTimeout(Long.parseLong(taskExecutionTimeout));
            } catch (NumberFormatException nfe) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Wrong configuration ==> Use default taskExecutionTimeout value of DefaultOfficeManagerConfiguration");
                }
            }
        }

        if (retryTimeout != null) {
            try {
                configuration.setRetryTimeout(Long.parseLong(retryTimeout));
            } catch (NumberFormatException nfe) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Wrong configuration ==> Use default retryTimeout value of DefaultOfficeManagerConfiguration");
                }
            }
        }

        if (maxTasksPerProcess != null) {
            try {
                configuration.setMaxTasksPerProcess(Integer.parseInt(maxTasksPerProcess));
            } catch (NumberFormatException nfe) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Wrong configuration ==> Use default maxTasksPerProcess value of DefaultOfficeManagerConfiguration");
                }
            }
        }

        try {
            officeManager = configuration.buildOfficeManager();
            documentConverter = new OfficeDocumentConverter(officeManager);
        } catch (IllegalStateException ise) {
            if (LOG.isErrorEnabled()) {
                LOG.equals(ise.getMessage());
            }
        }

        /*
		String officeProfileParam = servletContext.getInitParameter(PARAMETER_OFFICE_PROFILE);
		if (officeProfileParam != null) {
		    configuration.setTemplateProfileDir(new File(officeProfileParam));
		}
		*/
	}

	protected static void init(ServletContext servletContext) {
        //WebappContext instance = new WebappContext();

        ApplicationContext context= new ClassPathXmlApplicationContext( "applicationContext.xml ");

        WebappContext instance = (WebappContext)context.getBean("WebappContext");

        instance.WebappContextPostConstr(servletContext);
        servletContext.setAttribute(KEY, instance);
        try {
            if (instance.officeManager != null) {
                instance.officeManager.start();
            }
        } catch (OfficeException oe) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception when start Office Service: ",oe);
            }
        }
	}

	protected static void destroy(ServletContext servletContext) {
		WebappContext instance = get(servletContext);
        try {
            if (instance.officeManager != null) {
                instance.officeManager.stop();
            }
        } catch (OfficeException oe) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception when stop Office Service: ",oe);
            }
        }
	}

	public static WebappContext get(ServletContext servletContext) {
		return (WebappContext) servletContext.getAttribute(KEY);
	}

	public ServletFileUpload getFileUpload() {
		return fileUpload;
	}

	public static OfficeManager getOfficeManager() {
        return officeManager;
    }

	public static OfficeDocumentConverter getDocumentConverter() {
        return documentConverter;
    }

}

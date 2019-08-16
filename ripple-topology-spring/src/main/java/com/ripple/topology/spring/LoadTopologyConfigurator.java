package com.ripple.topology.spring;

import com.ripple.topology.Resource;
import com.ripple.topology.Topology;
import com.ripple.topology.TopologyFactory;
import com.ripple.topology.serialization.TopologyMarshaller;
import com.ripple.topology.spring.util.TopologySynchronousLifecycleManager;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

public class LoadTopologyConfigurator implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(LoadTopologyConfigurator.class);
    private ResourceLoader resourceLoader;
    
    @Override
    public void registerBeanDefinitions(final AnnotationMetadata annotationMetadata,
        final BeanDefinitionRegistry registry) {

        if (annotationMetadata.hasAnnotation(LoadTopology.List.class.getName())) {
            AnnotationAttributes annotationAttributesList = new AnnotationAttributes(
                annotationMetadata.getAnnotationAttributes(LoadTopology.List.class.getName()));

            for (AnnotationAttributes annotationAttributes : annotationAttributesList.getAnnotationArray("value")) {
                process(annotationAttributes, registry);
            }
        } else {
            AnnotationAttributes annotationAttributes = new AnnotationAttributes(
                annotationMetadata.getAnnotationAttributes(LoadTopology.class.getName()));
            process(annotationAttributes, registry);
        }
    }

    private void process(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        String topologyName = annotationAttributes.getString("name");
        String yamlPath = annotationAttributes.getString("yaml");
        Class<?> topologyFactoryClass = annotationAttributes.getClass("factory");

        Topology topology = null;
        if (TopologyFactory.class.isAssignableFrom(topologyFactoryClass) && !topologyFactoryClass
            .equals(TopologyFactory.class)) {
            topology = loadFromClass(topologyFactoryClass);
        } else if (!(yamlPath.equals(""))) {
            topology = loadFromResource(resourceLoader.getResource(yamlPath));
        } else {
            throw new RuntimeException("@LoadTopology requires either a valid TopologyFactory or YAML resource"
                + " specified in order to load a Topology.");
        }
        topology.registerShutdownHook();

        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
        if (beanFactory.containsBean(topologyName)) {
            logger.info("Cached topology '{}' found.  Reusing existing topology.");
            return;
        }
        logger.info("Registering '{}' topology", topologyName);
        beanFactory.registerSingleton(topologyName, topology);

        registerLifecycleManager(topologyName, registry);

        topology.start().join();

        for (Resource resource : topology.getElements(Resource.class)) {
            logger.info("Registering '{}' topology resource '{}' ({})", topologyName, resource.getKey(),
                resource.getClass());
            beanFactory.registerSingleton(resource.getKey(), resource);
        }
    }

    private Topology loadFromResource(org.springframework.core.io.Resource yamlResource) {
        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();
        try {
            logger.info("Loading Topology from {}", yamlResource);
            return marshaller.read(yamlResource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error loading from YAML resource " + yamlResource, e);
        }
    }

    private Topology loadFromClass(Class<?> factoryType) {
        TopologyFactory factory = null;
        try {
            logger.info("Loading Topology from {}", factoryType);
            factory = (TopologyFactory) factoryType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error loading TopologyFactory", e);
        }
        return factory.create();
    }

    private void registerLifecycleManager(String topologyName, BeanDefinitionRegistry registry) {
        logger.info("Registering '{}' topology lifecycle manager", topologyName);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(TopologySynchronousLifecycleManager.class)
            .setLazyInit(false);
        builder.addConstructorArgReference(topologyName);
        builder.setDestroyMethodName("stop");
        BeanDefinitionReaderUtils.registerWithGeneratedName(builder.getBeanDefinition(), registry);
    }

    @Override
    public void setResourceLoader(final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}

package com.kuangcao.ninja.vertx.standalone.utils;

import com.google.common.base.Optional;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaProperties;

public class ClassUtils {


    public static boolean doesClassExist(Class<?> claz, String nameWithPackage) {

        try {
            Class.forName(nameWithPackage, false, claz
                    .getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }

    }

    public static String resolveApplicationClassName(NinjaProperties ninjaProperties, String classLocationAsDefinedByNinja) {
        Optional<String> applicationModulesBasePackage
                = Optional.fromNullable(ninjaProperties.get(
                NinjaConstant.APPLICATION_MODULES_BASE_PACKAGE));

        if (applicationModulesBasePackage.isPresent()) {
            return applicationModulesBasePackage.get() +
                    '.' +
                    classLocationAsDefinedByNinja;
        } else {
            return classLocationAsDefinedByNinja;
        }
    }

}

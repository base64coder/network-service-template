package com.dtc.framework.beans.context;

import com.dtc.framework.beans.annotation.Condition;
import com.dtc.framework.beans.annotation.Profile;
import java.lang.reflect.AnnotatedElement;

public class ProfileCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedElement metadata) {
        if (context.getEnvironment() == null) {
            return true;
        }
        
        if (metadata.isAnnotationPresent(Profile.class)) {
            Profile profile = metadata.getAnnotation(Profile.class);
            return context.getEnvironment().acceptsProfiles(profile.value());
        }
        
        return true;
    }
}


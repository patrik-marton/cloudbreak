package com.sequenceiq.distrox.api.v1.distrox.model.upgrade;

import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.sequenceiq.common.api.util.ValidatorUtil;

public class DistroXUpgradeRequestValidator implements ConstraintValidator<ValidUpgradeRequest, DistroXUpgradeV1Request> {

    @Override
    public boolean isValid(DistroXUpgradeV1Request value, ConstraintValidatorContext context) {
        if (!validateEmptyRequest(value, context)) {
            return false;
        }
        if (!mutuallyExclusiveDryRunOrShowImages(value, context)) {
            return false;
        }
        return isOsUpgrade(value) || isRuntimeUpgrade(value) || value.isDryRunOnly() || value.isShowAvailableImagesOnly() || value.isEmpty();
    }

    private boolean validateEmptyRequest(DistroXUpgradeV1Request value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) {
            String msg = "Invalid upgrade request: empty content";
            ValidatorUtil.addConstraintViolation(context, msg).disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean mutuallyExclusiveDryRunOrShowImages(DistroXUpgradeV1Request request, ConstraintValidatorContext context) {
        if (request.isDryRun()  && request.isShowAvailableImagesSet()) {
            String msg = "Invalid upgrade request: 'dry-run' cannot be used in parallel with  'show-available-images' or "
                    + "'show-latest-available-image-per-runtime' in the request";
            ValidatorUtil.addConstraintViolation(context, msg).disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean isOsUpgrade(DistroXUpgradeV1Request request) {
        return Boolean.TRUE.equals(request.getLockComponents()) && StringUtils.isEmpty(request.getRuntime());
    }

    private boolean isRuntimeUpgrade(DistroXUpgradeV1Request request) {
        return !Boolean.TRUE.equals(request.getLockComponents())
                && (!StringUtils.isEmpty(request.getRuntime()) && StringUtils.isEmpty(request.getImageId())
                || !StringUtils.isEmpty(request.getImageId()) && StringUtils.isEmpty(request.getRuntime()));
    }
}

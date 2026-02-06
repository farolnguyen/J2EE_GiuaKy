package fit.hutech.spring.constants;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Provider {
    LOCAL("Local"),
    GOOGLE("Google"),
    GITHUB("GitHub"),
    FACEBOOK("Facebook");

    public final String value;
}
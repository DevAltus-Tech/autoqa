package com.kagr.videos.validator.reports;





import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;





@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestStatus {
    private String testName;
    private String status;
    private String notes;





    @Override
    public int hashCode() {
        return testName != null ? testName.hashCode() : 0;
    }





    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestStatus that = (TestStatus) o;
        return StringUtils.equals(testName, that.testName);
    }
}

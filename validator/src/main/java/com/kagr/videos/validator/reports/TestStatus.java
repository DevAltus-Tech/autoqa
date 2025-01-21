package com.kagr.videos.validator.reports;





import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;





@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestStatus {
    private String testName;
    private String status;
    private String notes;
}

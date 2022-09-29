package com.ca.classattender;

import org.jetbrains.annotations.NotNull;

public class SlotModel {
    int subCode, slotTemplate;
    String subName, slotTime, subTeacher;

    public SlotModel(@NotNull int slotTemplate, String subName, String slotTime, int subCode, String subTeacher){
        this.slotTemplate = slotTemplate;
        this.subName = subName;
        this.slotTime = slotTime;
        this.subCode = subCode;
        this.subTeacher = subTeacher;
    }
}

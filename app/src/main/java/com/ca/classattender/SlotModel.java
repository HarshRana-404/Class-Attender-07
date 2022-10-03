package com.ca.classattender;

import org.jetbrains.annotations.NotNull;

public class SlotModel {
    int slotTemplate;
    String subName, slotTime, subCode, subTeacher;

    public SlotModel(@NotNull int slotTemplate, String subName, String slotTime, String subCode, String subTeacher){
        this.slotTemplate = slotTemplate;
        this.subName = subName;
        this.slotTime = slotTime;
        this.subCode = subCode;
        this.subTeacher = subTeacher;
    }
}

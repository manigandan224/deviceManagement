package com.grootan.assetManagement.Model;

import jdk.jfr.Timestamp;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.sql.Date;


@Entity(name = "Device")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "manufacturedId"))
public class Device {
        @Id
        @Column(name="DeviceId")
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Integer DeviceId;

        @Column(name="manufacturedId")

        private String manufacturedId;

        @Column(name = "category")
        private String category;

        @Column(name = "deviceName")
        private String deviceName;

        @Column(name = "devicePurchaseDate")
        private Date devicePurchaseDate;

        @Column(name = "assign_status")
        private String assignStatus;

        @Column(name = "deviceStatus")
        private String deviceStatus;

        public Integer getDeviceId() {
                return DeviceId;
        }

        public void setDeviceId(Integer deviceId) {
                DeviceId = deviceId;
        }

        public String getManufacturedId() {
                return manufacturedId;
        }

        public void setManufacturedId(String manufacturedId) {
                this.manufacturedId = manufacturedId;
        }

        public String getCategory() {
                return category;
        }

        public void setCategory(String category) {
                this.category = category;
        }

        public String getDeviceName() {
                return deviceName;
        }

        public void setDeviceName(String deviceName) {
                this.deviceName = deviceName;
        }

        public Date getDevicePurchaseDate() {
                return devicePurchaseDate;
        }

        public void setDevicePurchaseDate(Date devicePurchaseDate) {
                this.devicePurchaseDate = devicePurchaseDate;
        }

        public String getAssignStatus() {
                return assignStatus;
        }

        public void setAssignStatus(String assignStatus) {
                this.assignStatus = assignStatus;
        }

        public String getDeviceStatus() {
                return deviceStatus;
        }

        public void setDeviceStatus(String deviceStatus) {
                this.deviceStatus = deviceStatus;
        }

        public Device(Integer DeviceId, String manufacturedId, String category, String deviceName, Date devicePurchaseDate, String assignStatus, String deviceStatus) {
                this.DeviceId=DeviceId;
                this.manufacturedId = manufacturedId;
                this.category = category;
                this.deviceName = deviceName;
                this.devicePurchaseDate = devicePurchaseDate;
                this.assignStatus = assignStatus;
                this.deviceStatus = deviceStatus;
        }

        public Device(String manufacturedId, String category, String deviceName, Date devicePurchaseDate, String assignStatus, String deviceStatus) {
                this.manufacturedId = manufacturedId;
                this.category = category;
                this.deviceName = deviceName;
                this.devicePurchaseDate = devicePurchaseDate;
                this.assignStatus = assignStatus;
                this.deviceStatus = deviceStatus;
        }

        public Device(Integer DeviceId) {
                this.DeviceId = DeviceId;
        }
        public Device()
        {

        }

}
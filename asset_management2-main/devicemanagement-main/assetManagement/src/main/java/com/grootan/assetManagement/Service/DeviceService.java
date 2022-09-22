package com.grootan.assetManagement.Service;

import com.google.gson.Gson;
import com.grootan.assetManagement.Exception.AlreadyExistsException;
import com.grootan.assetManagement.Exception.FieldEmptyException;
import com.grootan.assetManagement.Exception.GeneralException;
import com.grootan.assetManagement.Exception.ResourceNotFoundException;
import com.grootan.assetManagement.Model.*;
import com.grootan.assetManagement.Repository.*;
import com.grootan.assetManagement.Response;
import com.grootan.assetManagement.request.DeviceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.logging.Logger;

import static com.grootan.assetManagement.Model.Constants.*;

@Service
public class DeviceService {
    String UserName;


    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private DeviceDao deviceDao;

    @Autowired
    private DeviceCategoryDao deviceCategoryDao;

    @Autowired
    HistoryDao historyDao;

    @Autowired
    private CommonService service;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DeviceNameDao deviceNameDao;

    Logger logger = Logger.getLogger("com.grootan.assetManagement.Service");

    //get All device by device name
    public List<String> getAllDevicesByName()
    {
        return (List<String>) deviceDao.getDeviceByName();
    }

    //to check device Id  already exits or not
    private boolean deviceIdExists(final String id)
    {
        return deviceDao.findByDeviceId(id)!=null;
    }

    //to check device name exits or not
    private boolean deviceNameExists(final String name)
    {
        return deviceNameDao.findByDeviceName(name)!=null;
    }

    //to check device category already exits or  not
    private boolean deviceCategoryExists(final String category)
    {
        return deviceCategoryDao.findByDeviceCategory(category)!=null;
    }


    public void emptyFieldCheck(DeviceRequest deviceRequest) throws FieldEmptyException {
        if(deviceRequest.getCategory().isEmpty()||deviceRequest.getDeviceName().isEmpty()||
                deviceRequest.getManufacturedId().isEmpty()||deviceRequest.getAssignStatus().isEmpty()||
                deviceRequest.getDevicePurchaseDate()==null||deviceRequest.getDeviceStatus().isEmpty())
        {
            throw new FieldEmptyException("field should not empty");
        }
    }
    /**
     * Add device details
     */
    public ResponseEntity<Object> addDeviceDetails(DeviceRequest deviceRequest) throws FieldEmptyException
    {
        emptyFieldCheck(deviceRequest);
        if(deviceIdExists(deviceRequest.getManufacturedId()))
        {
            throw new GeneralException("manufactured id already exits");
        }

        saveHistory(deviceRequest,DEVICE_ADD);
        Device device=new Device(deviceRequest.getManufacturedId(), deviceRequest.getCategory(),
                deviceRequest.getDeviceName(),deviceRequest.getDevicePurchaseDate(),
                deviceRequest.getAssignStatus(),deviceRequest.getDeviceStatus());

        deviceDao.save(device);

        return new ResponseEntity<>(
                new Response<>(String.valueOf(HttpStatus.CREATED.value()), HttpStatus.CREATED.getReasonPhrase(),
                        "successfully saved",deviceRequest),
                new HttpHeaders(),
                HttpStatus.CREATED);
    }

    //update device details
    public ResponseEntity<Object> updateDeviceDetails(DeviceRequest deviceRequest) throws FieldEmptyException {

        emptyFieldCheck(deviceRequest);

        Device device=deviceDao.getDeviceId(deviceRequest.getDeviceId());
        device.setDeviceName(deviceRequest.getDeviceName());
        device.setDeviceStatus(deviceRequest.getDeviceStatus());
        device.setCategory(deviceRequest.getCategory());
        device.setDevicePurchaseDate(deviceRequest.getDevicePurchaseDate());
        device.setAssignStatus(deviceRequest.getAssignStatus());
        device.setManufacturedId(deviceRequest.getManufacturedId());

        saveHistory(deviceRequest,DEVICE_UPDATED);

        deviceDao.save(device);

        return new ResponseEntity<>(
                new Response<>(String.valueOf(HttpStatus.OK.value()), HttpStatus.OK.getReasonPhrase(), "Updated saved",deviceRequest),
                new HttpHeaders(),
                HttpStatus.OK);
    }


    //get all devices
    public ResponseEntity<Object> getAllDevices() throws ResourceNotFoundException {
        List<Device> deviceList=deviceDao.findAll();
        if (deviceList.isEmpty())
        {
            throw new ResourceNotFoundException("device not found");
        }

        return   new ResponseEntity<>(
                new Response<>(String.valueOf(HttpStatus.FOUND.value()), HttpStatus.FOUND.getReasonPhrase(), "device found", deviceList),
                new HttpHeaders(),
                HttpStatus.FOUND);
    }


    //get device by device id
    public ResponseEntity<Object> findDeviceById(int id)
    {
        Optional<Device> device = deviceDao.findById(id);
        return device.map(value -> new ResponseEntity(
                new Response<>(String.valueOf(HttpStatus.OK.value()), HttpStatus.OK.getReasonPhrase(), "device found", value),
                new HttpHeaders(),
                HttpStatus.OK)).orElseGet(() -> new ResponseEntity(
                new Response<>(String.valueOf(HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND.getReasonPhrase(), "device not found"),
                new HttpHeaders(),
                HttpStatus.NOT_FOUND
        ));
    }

    /**
     * search by keyword using i like
     * search device by name or id using search button
    */
    public List<Device> getByKeywordDevice(String keyword)
    {
        return deviceDao.findByKeyword(keyword);
    }

    //save device category
    public ResponseEntity<Object> saveDeviceCategory(DeviceCategory category) throws FieldEmptyException, AlreadyExistsException {
        String lowerCase = category.getCategory().toLowerCase();
        if(category.getCategory().isEmpty())
        {
            throw new FieldEmptyException("field should not empty");
        }

        if(deviceCategoryExists(lowerCase))
        {
            throw new AlreadyExistsException("This category is already exists: "+category.getCategory());
        }

        History history=new History(service.currentUser(),DEVICE_CATEGORY_ADD,new Gson().toJson(category),service.DateAndTime());
        historyDao.save(history);

        category.setCategory(lowerCase);

        deviceCategoryDao.save(category);
        return new ResponseEntity<>(
                new Response<>(String.valueOf(HttpStatus.CREATED.value()), HttpStatus.CREATED.getReasonPhrase(), "successfully saved",category),
                new HttpHeaders(),
                HttpStatus.CREATED);
    }



    //get device category
    public ResponseEntity<Object> getCategory() throws ResourceNotFoundException
    {
        List<DeviceCategory> deviceCategoryList=deviceCategoryDao.findAll();
        if(deviceCategoryList.isEmpty())
        {
            throw new ResourceNotFoundException("no device found");
        }
        return   new ResponseEntity<>(
                new Response<>(String.valueOf(HttpStatus.OK.value()), HttpStatus.OK.getReasonPhrase(),
                        "device found", deviceCategoryList),
                new HttpHeaders(),
                HttpStatus.OK);
    }

    //save device name
    public ResponseEntity<Object> saveDeviceName(DeviceName deviceName) throws FieldEmptyException, AlreadyExistsException {
        DeviceCategory deviceCategory = deviceName.getDeviceCategory();
        String device= deviceCategory.getCategory();
        long id = deviceCategoryDao.findByDeviceCategoryId(device);
        String lowerCase = deviceName.getName().toLowerCase();
        if(deviceName.getName()==""||deviceCategory.getCategory()=="")
        {
            throw new FieldEmptyException("field should not empty");
        }
        if(deviceNameExists(lowerCase))
        {
            throw new AlreadyExistsException("This device Name is Already Exists: "+deviceName.getName());
        }

        deviceName.setName(lowerCase);
        deviceName.setDeviceCategory(new DeviceCategory(id,device));
        saveHistory(deviceName,DEVICE_NAME_ADD);
        deviceName.setName(lowerCase);

        deviceNameDao.save(deviceName);
        return new ResponseEntity<>(
                new Response<>(String.valueOf(HttpStatus.CREATED.value()),
                        HttpStatus.CREATED.getReasonPhrase(), "successfully saved",deviceName),
                new HttpHeaders(),
                HttpStatus.CREATED);
    }

    public void saveHistory(Object o,String constant)
    {
        String userName=service.currentUser();
        History saveHistory=new History(userName,constant,new Gson().toJson(o),service.DateAndTime());
        historyDao.save(saveHistory);

    }

    //get device name
    public ResponseEntity<Object> getDeviceName() throws ResourceNotFoundException {
        List<DeviceName> list=deviceNameDao.findAll();
        if(list.isEmpty())
        {
            throw new ResourceNotFoundException("no device found");
        }
        return new ResponseEntity<>(
                new Response<>(String.valueOf(HttpStatus.FOUND.value()),
                        HttpStatus.FOUND.getReasonPhrase(), "device name found", list),
                new HttpHeaders(),
                HttpStatus.OK);
    }

    public ResponseEntity<Object> getHistory() throws ResourceNotFoundException {
        List<History> list=historyDao.findAll();
        if(list.isEmpty())
        {
            throw new ResourceNotFoundException("history not found");

        }
        return   new ResponseEntity<>(
                new Response<>(String.valueOf(HttpStatus.FOUND.value()), HttpStatus.FOUND.getReasonPhrase(), "history  found", list),
                new HttpHeaders(),
                HttpStatus.FOUND);

    }


    //pagination by employee table
    public Page<History> findPaginated(int pageNo, int pageSize)
    {
        Pageable pageable = PageRequest.of(pageNo-1,pageSize);
        return this.historyDao.findAll(pageable);
    }

    public List<DeviceCategory> getAllDeviceCategory()
    {
        return deviceCategoryDao.findAll();
    }

    public ResponseEntity<Object> deleteDeviceCategory(String id) throws ResourceNotFoundException {
        DeviceCategory deviceCategory=deviceCategoryDao.findByDeviceCategory(id);
        if(deviceCategory==null)
        {
            throw new ResourceNotFoundException("no device category found");
        }
        deviceCategoryDao.deleteById(id);

        return   new ResponseEntity<>(
                new Response<>(String.valueOf(HttpStatus.OK.value()), HttpStatus.OK.getReasonPhrase(), "deleted successful"),
                new HttpHeaders(),
                HttpStatus.OK);
    }

    public ResponseEntity<Object> deleteDeviceDetails(Integer id) throws ResourceNotFoundException {
        Device device=deviceDao.getByDeviceId(id);
        if(device==null)
        {
            throw new ResourceNotFoundException("device not found");

        }

        saveHistory(device,DEVICE_DELETE);
        deviceDao.deleteForiegnKey(id);
        deviceDao.deleteById(id);
        return new ResponseEntity<>(
                new Response<>(String.valueOf(HttpStatus.OK.value()),
                        HttpStatus.OK.getReasonPhrase(), "deleted successful"),
                new HttpHeaders(), HttpStatus.OK);
    }

    public List<String> findByCategory(long id) {
        return deviceNameDao.getDeviceNames(id);
    }

    public int findByCategoryId(String name)
    {
        return deviceNameDao.getDeviceCategoryId(name);
    }

    public Object findAll() {
        return deviceCategoryDao.findAll();
    }
}
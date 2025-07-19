package com.pixeltribe.shopsys.proSerialNumber.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductRepository;

@Service
public class ProSerialNumberService {
    @Autowired
    ProSerialNumberRepository proSerialNumberRepository;
    @Autowired
    ProductRepository productRepository;
    
    @Transactional
    public ProSerialNumber addProSN(ProSerialNumber proSN) {
        return proSerialNumberRepository.save(proSN);
    }
    @Transactional
    public ProSerialNumber updateProSN(ProSerialNumber proSN) {
        return proSerialNumberRepository.save(proSN);
    }

    public void delete(ProSerialNumber proSN) {
        proSerialNumberRepository.deleteById(proSN.getId());
    }

    public ProSerialNumber getOneProSN(Integer proSNNo) {
        Optional<ProSerialNumber> optional = proSerialNumberRepository.findById(proSNNo);
        return optional.orElse(null);
    }
    @Transactional
    public Integer addMultipleProSN(List<ProSerialNumber> proSNList) {
        List<ProSerialNumber> savedList = proSerialNumberRepository.saveAll(proSNList);
        return savedList.size();
    }
    
    public Integer countProducts(Integer proNo) {
        return proSerialNumberRepository.countProduct(proNo);
    }
    
    public Integer countStock(Integer proNo) {
        return proSerialNumberRepository.countStock(proNo);
    }
    
    public List<ProSerialNumber> findByProduct(Product product) {
        return proSerialNumberRepository.findByProNo(product);
    }

    public String importFile(MultipartFile file, Integer proNo) {
        try {
            // 基本驗證
            if (file == null || file.isEmpty()) {
                return "檔案不能為空";
            }
            
            if (proNo == null || proNo <= 0) {
                return "產品編號不正確";
            }
            
            // 預先查詢 Product 對象
            Product product = productRepository.findById(proNo).orElse(null);
            if (product == null) {
                return "找不到指定的產品";
            }
            
            List<String> serialNumbers = parseSerialNumbers(file);
            
            if (serialNumbers.isEmpty()) {
                return "檔案內容為空或格式不正確";
            }
            
            int successCount = 0;
            
            for (String serialNumber : serialNumbers) {
                try {
                        ProSerialNumber newSerial = new ProSerialNumber();
                        newSerial.setProductSn(serialNumber);
                        newSerial.setProNo(product);  // 現在使用 setProNo
                        newSerial.setOrderItemNo(null);
                        
                        proSerialNumberRepository.save(newSerial);
                        successCount++;
                } catch (Exception e) {
                    System.err.println("處理序號 " + serialNumber + " 時發生錯誤: " + e.getMessage());
                }
            }
            
            return String.format("新增: %d 筆", successCount);
            
        } catch (Exception e) {
            return "匯入失敗：" + e.getMessage();
        }
    }

    private List<String> parseSerialNumbers(MultipartFile file) {
        List<String> serialNumbers = new ArrayList<>();
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), "UTF-8"));
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    // 支援 CSV 格式（逗號分隔）
                    if (line.contains(",")) {
                        String[] serials = line.split(",");
                        for (String serial : serials) {
                            serial = serial.trim();
                            if (!serial.isEmpty()) {
                                serialNumbers.add(serial);
                            }
                        }
                    } else {
                        // 每行一個序號
                        serialNumbers.add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("讀取檔案時發生錯誤: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("關閉檔案時發生錯誤: " + e.getMessage());
                }
            }
        }
        
        return serialNumbers;
    }
}
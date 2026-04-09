package com.alem.GIA.DTO;

import com.alem.GIA.entity.Address;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDto {

    private String streetName;
    private String city;
    private String state;
    private String zipCode;

    public AddressDto(Address address) {
        this.streetName = address.getStreetName();
        this.city = address.getCity();
        this.state = address.getState();
        this.zipCode = address.getZipCode();
    }
}
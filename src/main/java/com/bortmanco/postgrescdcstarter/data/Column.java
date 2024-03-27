package com.bortmanco.postgrescdcstarter.data;


import com.bortmanco.postgrescdcstarter.data.enums.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column {

    private String name;
    private DataType dataType;

}

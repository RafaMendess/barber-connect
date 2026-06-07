package com.projeto.barberconnect.mapper;

import com.projeto.barberconnect.dto.barbershop.BarbershopResponseDto;
import com.projeto.barberconnect.dto.barbershop.CreateBarbershopRequestDto;
import com.projeto.barberconnect.dto.barbershop.UpdateBarbershopRequestDto;
import com.projeto.barberconnect.entity.Barbershop;
import com.projeto.barberconnect.entity.User;
import com.projeto.barberconnect.util.StringNormalizer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;

public final class BarbershopMapper {

    private static final int WGS84_SRID = 4326;

    private BarbershopMapper() {
    }

    public static Barbershop toEntity(CreateBarbershopRequestDto dto, User owner) {
        return toEntity(dto, owner, StringNormalizer.trim(dto.cnpj()));
    }

    public static Barbershop toEntity(CreateBarbershopRequestDto dto, User owner, String cnpj) {
        Barbershop barbershop = new Barbershop();
        barbershop.setCnpj(cnpj);
        barbershop.setName(StringNormalizer.trim(dto.name()));
        barbershop.setAddress(StringNormalizer.trim(dto.address()));
        barbershop.setPhone(StringNormalizer.trimToNull(dto.phone()));
        barbershop.setBusinessHours(StringNormalizer.trimToNull(dto.businessHours()));
        barbershop.setPhotoUrl(StringNormalizer.trimToNull(dto.photoUrl()));
        barbershop.setLocation(toPoint(dto.latitude(), dto.longitude()));
        barbershop.setOwner(owner);

        return barbershop;
    }

    public static void applyUpdate(UpdateBarbershopRequestDto dto, Barbershop barbershop) {
        if (dto.name() != null) {
            barbershop.setName(StringNormalizer.trim(dto.name()));
        }
        if (dto.phone() != null) {
            barbershop.setPhone(StringNormalizer.trimToNull(dto.phone()));
        }
        if (dto.address() != null) {
            barbershop.setAddress(StringNormalizer.trim(dto.address()));
        }
        if (dto.businessHours() != null) {
            barbershop.setBusinessHours(StringNormalizer.trimToNull(dto.businessHours()));
        }
        if (dto.photoUrl() != null) {
            barbershop.setPhotoUrl(StringNormalizer.trimToNull(dto.photoUrl()));
        }
        if (dto.latitude() != null && dto.longitude() != null) {
            barbershop.setLocation(toPoint(dto.latitude(), dto.longitude()));
        }
    }

    public static BarbershopResponseDto toResponse(Barbershop barbershop) {
        Coordinate coordinate = barbershop.getLocation() == null ? null : barbershop.getLocation().getCoordinate();

        return new BarbershopResponseDto(
                barbershop.getId(),
                barbershop.getName(),
                barbershop.getCnpj(),
                barbershop.getPhone(),
                barbershop.getAddress(),
                barbershop.getBusinessHours(),
                barbershop.getPhotoUrl(),
                toLatitude(coordinate),
                toLongitude(coordinate)
        );
    }

    private static Point toPoint(BigDecimal latitude, BigDecimal longitude) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(
                longitude.doubleValue(),
                latitude.doubleValue()
        ));
        point.setSRID(WGS84_SRID);

        return point;
    }

    private static BigDecimal toLatitude(Coordinate coordinate) {
        return coordinate == null ? null : BigDecimal.valueOf(coordinate.getY());
    }

    private static BigDecimal toLongitude(Coordinate coordinate) {
        return coordinate == null ? null : BigDecimal.valueOf(coordinate.getX());
    }
}

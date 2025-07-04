package kr.hhplus.be.server.infrastructure.persistence.keyword;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "key_region")
@NoArgsConstructor
@AllArgsConstructor
public class KeyRegionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "region")
    private String regionName;

    public KeyRegionEntity(String regionName) {
        this.regionName = regionName;
    }

    public Long getId() {
        return id;
    }

    public String getRegionName() {
        return regionName;
    }

    
}

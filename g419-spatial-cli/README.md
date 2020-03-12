# Actions

## print-spatial-objects

Sample run:
```bash
./spatial-cli print-spatial-objects -i tei:gz -f stuff/samples/pst20_00120445
```

Expected output:
```bash
Document,Annotation id,Spatial object,POS,Ctag,Named entity type,Named entity text,Orth and NE concepts,Mention,Mention text,Cluster size,Cluster concepts,Cluster bases
stuff/samples/pst20_00120445/,ann_annotations.xml#756592,Kaliszu,subst,subst:sg:loc:m3,nam_loc_gpe_city,Kaliszu,city,mention,Wczesnośredniowieczny Gród Piastów w Kaliszu-Zawodziu,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756590,Gród,subst,subst:sg:nom:m3,nam_fac_goe,Wczesnośredniowieczny Gród Piastów,"City, building",mention,Wczesnośredniowieczny Gród Piastów w Kaliszu-Zawodziu,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756595,dzielnicy,subst,subst:sg:loc:f,,,LandArea,mention,dzielnicy Kalisza,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756593,rezerwat,subst,subst:sg:nom:m3,,,"GeographicArea, GeopoliticalArea",mention,Ten niezwykły rezerwat archeologiczny,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756608,kolegiaty,subst,subst:sg:gen:f,,,ReligiousBuilding,mention,romańskiej kolegiaty,2,"Inside, Region, ReligiousBuilding","kolegiata, wnętrze"
stuff/samples/pst20_00120445/,ann_annotations.xml#756605,nimi,ppron3,ppron3:pl:inst:m1:ter:akc:npraep,,,,mention,nimi,2,City,"gród, on"
stuff/samples/pst20_00120445/,ann_annotations.xml#756603,trzcinie,subst,subst:sg:loc:f,,,Grass,mention,trzcinie,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756601,łodzie,subst,subst:pl:acc:f,,,,mention,łodzie,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756600,fosą,subst,subst:sg:inst:f,,,StationaryArtifact,mention,most drewniany nad fosą,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756598,most,subst,subst:sg:nom:m3,,,Bridge,mention,most drewniany nad fosą,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756597,grodu,subst,subst:sg:gen:m3,,,City,mention,grodu,5,City,gród
stuff/samples/pst20_00120445/,ann_annotations.xml#756611,zarys,subst,subst:sg:acc:m3,,,Plan,mention,zarys pierwszej,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756610,wnętrzu,subst,subst:sg:loc:n,,,"Inside, Region",mention,jej wnętrzu,2,"Inside, Region, ReligiousBuilding","kolegiata, wnętrze"
stuff/samples/pst20_00120445/,ann_annotations.xml#756616,makieta,subst,subst:sg:nom:f,,,Icon,mention,grób kurhanowy oraz makieta grodu,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756615,grób,subst,subst:sg:nom:m3,,,Tomb,mention,grób kurhanowy oraz makieta grodu,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756614,bramą,subst,subst:sg:inst:f,,,Door,mention,bramą,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756620,chat,subst,subst:pl:gen:f,,,"ResidentialBuilding, Address",mention,wczesnośredniowiecznych chat,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756619,niej,ppron3,ppron3:sg:gen:f:ter:akc:praep,,,,,,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756624,Mieszko,subst,subst:sg:nom:m1,nam_liv_person,Mieszko III Stary,human,mention,pochowany Mieszko III,,,
stuff/samples/pst20_00120445/,ann_annotations.xml#756622,krypcie,subst,subst:sg:loc:f,,,Room,mention,podziemnej krypcie,,,
```

## sumo-superclasses

Sample run:
```bash
./spatial-cli sumo-superclasses

Concept: vehicle
```

Expected output:
```bash
Superclasses: artifact, transportationdevice, physical, device, entity, object
```
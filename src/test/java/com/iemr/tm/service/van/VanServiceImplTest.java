package com.iemr.tm.service.van;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iemr.tm.data.van.MVan;
import com.iemr.tm.repo.van.VanRepo;
@ExtendWith(MockitoExtension.class)
class VanServiceImplTest {
	
	@InjectMocks
	VanServiceImpl vanServiceImpl;
	
	@Mock
	VanRepo vanRepo;

	@Test
	void testGetvan() {
		MVan mVan = new MVan();
		Integer id = 220;
		when(vanRepo.findByVanID(Mockito.any())).thenReturn(mVan);
		MVan getvan = vanServiceImpl.getvan(id);
		assertNotNull(getvan);
	}

}

#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <fstream>
#include <papi.h>

using namespace std;
#define SYSTEMTIME clock_t
std::ofstream fileTXT("resultsCpp.txt");
std::ofstream file2TXT("results2Cpp.txt");
std::ofstream file3TXT("results3Cpp.txt");

void OnMult(int m_ar, int m_br)  {
	
	SYSTEMTIME Time1, Time2;

	int numIteracoes = 10;
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	
	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

	for(int i = 0; i < m_ar*m_br; ++i){
		phc[i] = 0;
	}
	
    
	Time1 = clock();


	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			for( k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


	Time2 = clock();

	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	fileTXT << st;

	free(pha);
	free(phb);
	free(phc);
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br) {
    	
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;


	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

	for(int i = 0; i < m_ar*m_br; ++i){
		phc[i] = 0;
	}
    Time1 = clock();

	for(i=0; i<m_ar; i++)
	{	for(k=0; k<m_ar; k++)
		{	 
			for(j=0; j<m_br; j++)
			{	
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			
		}
	}

	Time2 = clock();

    sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	file2TXT << st;

	free(pha);
	free(phb);
	free(phc);
    
}

// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize)
{
    
    SYSTEMTIME Time1, Time2;
	
	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;
	
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);


    Time1 = clock();

	int num_blocks = m_ar/bkSize;
	
 
	for (int line_matrix_a = 0; line_matrix_a < num_blocks; line_matrix_a++) { //percorre as linhas de blocos da matriz A
		
		for(int block_index=0; block_index < num_blocks; block_index++) { //percorre os blocos da linha

			for(int col_matrix_b=0; col_matrix_b < num_blocks; col_matrix_b++) { //percorre as colunas de blocos da matriz b
				int next_line_a = (line_matrix_a + 1) * bkSize; //proxima linha de blocos da matriz A
				
				for(int i = line_matrix_a * bkSize; i < next_line_a; i++) { //percorre as linhas de um dos blocos da matriz A
					int next_block_a = (block_index + 1) * bkSize;

					for (int k = block_index * bkSize; k < next_block_a; k++) { //percorre as colunas do bloco
						int next_block_b = (col_matrix_b+1)*bkSize;

						for (int j = col_matrix_b * bkSize; j < next_block_b; j++) { //percorre as colunas de um bloco da matriz B
							phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_ar + j];
						}
					}
				}
			}
		}
	}

	Time2 = clock();

	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	file3TXT << st;

	free(pha);
	free(phb);
	free(phc);
}



void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main (int argc, char *argv[]) {

	char c;
	int lin, col, blockSize;
	int op;
	
	int EventSet = PAPI_NULL;
  	long long values[2];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;


	op=1;
	do {
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;

	
		switch (op){
			case 1: 
				fileTXT << "-----Multiplication-----" << endl;

				for(int i=600; i<=3000; i+=400){
					fileTXT << "Matrix size: " << i << "x" << i << endl;

					for(int j=0; j<10; j++){
						fileTXT << "Iteration: " << j+1 << endl;

						ret = PAPI_start(EventSet);
						if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

						OnMult(i,i);

						ret = PAPI_stop(EventSet, values);
						if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
						fileTXT << "L1 DCM: " << values[0] << endl;
						fileTXT << "L2 DCM: " << values[1] << endl;
						fileTXT << endl;
						ret = PAPI_reset( EventSet );
						if ( ret != PAPI_OK ) {
							std::cout << "FAIL reset" << endl; 
						}
					}
				}
				fileTXT.close();
				break;
			case 2:
				file2TXT << "-----Line Multiplication-----" << endl;

				for(int i=600; i<=3000; i+=400){
					file2TXT << "Matrix size: " << i << "x" << i << endl;

					for(int j=0; j<10; j++){
						file2TXT << "Iteration: " << j+1 << endl;

						ret = PAPI_start(EventSet);
						if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

						OnMultLine(i,i);

						ret = PAPI_stop(EventSet, values);
						if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
						file2TXT << "L1 DCM: " << values[0] << endl;
						file2TXT << "L2 DCM: " << values[1] << endl;
						file2TXT << endl;
						ret = PAPI_reset( EventSet );
						if ( ret != PAPI_OK ) {
							std::cout << "FAIL reset" << endl; 
						}
					}
				}
				file2TXT.close();
				break;
			case 3:
				file3TXT << "-----Block Multiplication-----" << endl;

				for(int i=4096; i<=10240; i+=2048){
					file3TXT << "Matrix size: " << i << "x" << i << endl;

					for(int k=128; k<=512; k=k*2){
						file3TXT << "Block size: " << k << "x" << k << endl;

						for(int j=0; j<10; j++){
							file3TXT << "Iteration: " << j+1 << endl;

							ret = PAPI_start(EventSet);
							if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

							OnMultBlock(i,i, k);
							
							ret = PAPI_stop(EventSet, values);
							if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
							file3TXT << "L1 DCM: " << values[0] << endl;
							file3TXT << "L2 DCM: " << values[1] << endl;
							file3TXT << endl;
							ret = PAPI_reset( EventSet );
							if ( ret != PAPI_OK ) {
								std::cout << "FAIL reset" << endl; 
							}
						}
					}
				}
				file3TXT.close();
				break;
		}
	} while (op != 0);

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

}
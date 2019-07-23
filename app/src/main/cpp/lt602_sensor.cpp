
#include "lt602_sensor.h"

#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include "Tlog.h"

//#include <utils/Log.h>


namespace lt602 {

	ResultCode Initialize() {
		return RC_OK;
	}

	void Uninitialize() {
	
	}

	// Sensor
	Sensor::Sensor()
		: mSocket(INVALID_SOCKET)
		, mMeasureThreadHandle(THREAD_INIT_HANDLE)
		, mMeasureContinuous(false)
		, mMeasureCallback(NULL)
		, mFrameBuffer(NULL) {		
	}

	Sensor::~Sensor() {
		stopMeasureContinuous();
		disconnect();		
	}

	ResultCode Sensor::connect(const char *remoteIp, int remotePort){
		return connect("0.0.0.0",4010,remoteIp,remotePort);
	}

	ResultCode Sensor::connect(const char *localIp, int localPort, const char *remoteIp, int remotePort) {
		if (mSocket != INVALID_SOCKET) {
			return RC_ALREADY_CONNECTED;
		}

		mSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
		if (mSocket < 0) {
			TLOGE("socket err %d", mSocket);
			mSocket = INVALID_SOCKET;
			return RC_FAILED;
		}

		struct sockaddr_in localAddress;
		memset(&localAddress, 0, sizeof(localAddress));
		localAddress.sin_family = AF_INET;
		localAddress.sin_port = htons(localPort);
		localAddress.sin_addr.s_addr = inet_addr(localIp);
		int res = bind(mSocket, (struct sockaddr *)&localAddress, sizeof(localAddress));
		if (res != 0) {
			close(mSocket);
			mSocket = INVALID_SOCKET;
            TLOGE("bind err %d", res);
			return RC_ADDRESS_BIND_FAILED;
		}

		memset(&mSensorAddress, 0, sizeof(mSensorAddress));
		mSensorAddress.sin_family = AF_INET;
		mSensorAddress.sin_port = htons(remotePort);
		mSensorAddress.sin_addr.s_addr = inet_addr(remoteIp);

		struct timeval timeout = {TIME_OUT, 0};
		res = setsockopt(mSocket, SOL_SOCKET, SO_RCVTIMEO, (const char *)&timeout, sizeof(struct timeval));
		if (res != 0)
		{
			
close(mSocket);
			mSocket = INVALID_SOCKET;
			return RC_ADDRESS_BIND_FAILED;
		}
		res = setsockopt(mSocket, SOL_SOCKET, SO_SNDTIMEO, (const char *)&timeout, sizeof(struct timeval));		
		if (res != 0)
		{
			
close(mSocket);
			mSocket = INVALID_SOCKET;
			return RC_ADDRESS_BIND_FAILED;
		}

		// ???????????????
		mFrameBuffer = new uint8[MAX_FRAME_LENGTH];

		//// ????υτ??
		//uint8 queryState[3] = { 0x60, 0x00, 0x00 };
		//ResultCode rc = sendCommand(queryState, 3);
		//if (rc == RC_OK) {
		//	rc = receiveFrame(mFrameBuffer, 0, NULL);
		//}
		//if (rc != RC_OK) {
		//	closesocket(mSocket);
		//	mSocket = INVALID_SOCKET;
		//	delete[] mFrameBuffer;
		//	mFrameBuffer = NULL;
		//	return RC_COMMUNICATION_FAILED;
		//}
		
		return RC_OK;
	}

	ResultCode Sensor::setIntegrationTime(int time) {
		
		if (time < 0) {
			return RC_INVALID_ARGUMENT;
		}
		if (mSocket == INVALID_SOCKET) {
			return RC_NOT_CONNECTED;
		}
		if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
			return RC_IN_PROGRESS;
		}
		
		uint16 regValue1 = time * 48 / 65535 + 1;
		uint16 regValue2 = time * 48 / regValue1 - 1;

		uint8 command[4];
		command[0] = 0x07;
		command[1] = 0;
		// ????A0?????
		command[2] = 0xA0;
		command[3] = (uint8)(regValue1 >> 8);
		ResultCode rc = sendCommand(command, 4);
		if (rc != RC_OK) {
			return rc;
		}

		rc = receiveFrame(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}
		// ????A1?????
		command[2] = 0xA1;
		command[3] = (uint8)regValue1;
		rc = sendCommand(command, 4);
		if (rc != RC_OK) {
			return rc;
		}
		rc = receiveFrame(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}
		// ????A2?????
		command[2] = 0xA2;
		command[3] = (uint8)(regValue2 >> 8);
		rc = sendCommand(command, 4);
		if (rc != RC_OK) {
			return rc;
		}
		rc = receiveFrame(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}
		// ????A3?????
		command[2] = 0xA3;
		command[3] = (uint8)regValue2;
		rc = sendCommand(command, 4);
		if (rc != RC_OK) {
			return rc;
		}
		rc = receiveFrame(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}
		return RC_OK;
	}
	ResultCode Sensor::setTwiceIntegrationTime(int time1, int time2) {
		if (time1 < 0 || time2 < 0) {
			return RC_INVALID_ARGUMENT;
		}
		if (mSocket == INVALID_SOCKET) {
			return RC_NOT_CONNECTED;
		}
		if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
			return RC_IN_PROGRESS;
		}

		uint8 command[5] = { 0x30, (uint8)(time1 % 256), (uint8)(time1 / 256), (uint8)(time2 % 256), (uint8)(time2 / 256) };
		ResultCode rc = sendCommand(command, 5);
		if (rc != RC_OK) {
			return rc;
		}
		rc = receiveInter(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}
		return RC_OK;

	}

	ResultCode Sensor::receiveInter(uint8 *frame, int offset, int *extraLength) {
		for (;;) {
			if (offset >= 5) {
				if (frame[0] == 0xAA && frame[1] == 0x55) {
					int dataLength = frame[2] | (frame[3] << 8);
					// fixed
					if (offset == dataLength + 3) {
						break;
					}
				}
			}
			sockaddr_in remoteAddress;
			int remoteAddressLength = sizeof(remoteAddress);
			int rc = recvfrom(mSocket, (char *)frame + offset, MAX_FRAME_LENGTH - offset, 0,
				(struct sockaddr *)&remoteAddress, (socklen_t *)&remoteAddressLength);
			if (rc <= 0) {
				return rc == 0 ? RC_CONNECTION_CLOSED : RC_COMMUNICATION_FAILED;
			}
			if (remoteAddress.sin_port == mSensorAddress.sin_port &&
				remoteAddress.sin_addr.s_addr == mSensorAddress.sin_addr.s_addr) {
				offset += rc;
			}
		}
		return RC_OK;
	}

	ResultCode Sensor::setLD(int gear) {
		if (gear != 4 && gear != 5) {
			return RC_INVALID_ARGUMENT;
		}
		if (mSocket == INVALID_SOCKET) {
			return RC_NOT_CONNECTED;
		}
		if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
			return RC_IN_PROGRESS;
		}

		uint8 command[2] = { 0x0B, (uint8)gear };
		ResultCode rc = sendCommand(command, 2);
		if (rc != RC_OK) {
			return rc;
		}
		rc = receiveFrame(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}
		return RC_OK;
	}

	ResultCode Sensor::getCurrentState(int *state) {
		if (state == NULL) {
			return RC_INVALID_ARGUMENT;
		}
		if (mSocket == INVALID_SOCKET) {
			return RC_NOT_CONNECTED;
		}
		if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
			return RC_IN_PROGRESS;
		}
		uint8 command[3] = { 0x60, 0x00, 0x00 };
		ResultCode rc = sendCommand(command, 3);
		if (rc != RC_OK) {
			return rc;
		}
		rc = receiveFrame(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}
		if (mFrameBuffer[5] != 0x60) {
			return RC_INVALID_RESPONSE;
		}
		*state = mFrameBuffer[6] | (mFrameBuffer[7] << 8);

		return RC_OK;
	}

	bool Sensor::isConnected() const {
		return mSocket != INVALID_SOCKET;
	}

	ResultCode Sensor::acquireDistanceData(uint16 *data, int *length) {
		if (length == NULL) {
			return RC_INVALID_ARGUMENT;
		}
		if (mSocket == INVALID_SOCKET) {
			return RC_NOT_CONNECTED;
		}
		if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
			return RC_IN_PROGRESS;
		}
		uint8 enableShutter[2] = { 0x12, 0x01 };
		ResultCode rc = sendCommand(enableShutter, 2);
		if (rc != RC_OK) {
			return rc;
		}
		rc = receiveFrame(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}
		int dataLength = mFrameBuffer[2] | (mFrameBuffer[3] << 8);
		if (dataLength != 643) {
			return RC_INVALID_RESPONSE;
		}
		int dataCopy = (*length) * 2;
		if (dataCopy > dataLength - 3) {
			dataCopy = dataLength - 3;
		}
		memcpy(data, mFrameBuffer + 6, dataCopy);
		*length = dataCopy / 2;

		return RC_OK;
	}

	ResultCode Sensor::setPositionData(const uint16 *data, int length) {
		if (length != 320) {
			return RC_INVALID_ARGUMENT;
		}
		if (mSocket == INVALID_SOCKET) {
			return RC_NOT_CONNECTED;
		}
		if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
			return RC_IN_PROGRESS;
		}
		uint8 command[641];
		command[0] = 0x61;
		memcpy(command + 1, data, 640);
		ResultCode rc = sendCommand(command, 641);
		if (rc != RC_OK) {
			return rc;
		}
		rc = receiveFrame(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}		
		if (mFrameBuffer[6] != 0x01) { // ???¨®??????1
			return RC_INVALID_RESPONSE;
		}
		return RC_OK;
	}

	ResultCode Sensor::acquirePositionData(uint16 *data, int *length) {
		if (length == NULL) {
			return RC_INVALID_ARGUMENT;
		}
		if (mSocket == INVALID_SOCKET) {
			return RC_NOT_CONNECTED;
		}
		if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
			return RC_IN_PROGRESS;
		}
		uint8 command[2] = { 0x62, 0x00 };
		ResultCode rc = sendCommand(command, 2);
		if (rc != RC_OK) {
			return rc;
		}
		rc = receiveFrame(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}
		int dataLength = mFrameBuffer[2] | (mFrameBuffer[3] << 8);
		if (dataLength != 643) {
			return RC_INVALID_RESPONSE;
		}
		int dataCopy = (*length) * 2;
		if (dataCopy > dataLength - 3) {
			dataCopy = dataLength - 3;
		}
		memcpy(data, mFrameBuffer + 6, dataCopy);
		*length = dataCopy / 2;

		return RC_OK;
	}

	ResultCode Sensor::startMeasureContinuous(MeasureCallback *callback) {
		
		if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
			return RC_IN_PROGRESS;
		}
		if (callback == NULL) {
			return RC_INVALID_ARGUMENT;
		}
		
		mMeasureContinuous = true;
		mMeasureCallback = callback;

		mMeasureThreadHandle = pthread_create(&mMeasureThread, NULL, Sensor::MeasureThreadProc, this);
		if (mMeasureThreadHandle != THREAD_SUCC_HANDLE) {
			return RC_FAILED;
		}
		
		// ????¦Λ???????????????
		uint8 enableContinuousMode[2] = { 0x16, 0x01 };
		ResultCode rc = sendCommand(enableContinuousMode, 2);
		if (rc != RC_OK) {
			// ?????????
			mMeasureContinuous = false;
			//pthread_cancel(mMeasureThread);
			//mMeasureThreadHandle = THREAD_INIT_HANDLE;
			return rc;
		}

		return RC_OK;
	}

	bool Sensor::isMeasuringContinuous() const {
		return mMeasureThreadHandle != THREAD_INIT_HANDLE;
	}

	void Sensor::stopMeasureContinuous() {
		mMeasureContinuous = false;

		//if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
		//	pthread_cancel(mMeasureThread);
		//	mMeasureThreadHandle = THREAD_INIT_HANDLE;
		//}
	
		if (mSocket != INVALID_SOCKET) {
			// ????????????
			uint8 disableContinuousMode[2] = { 0x16, 0x00 };
			sendCommand(disableContinuousMode, 2);
		}
	}

	void Sensor::disconnect() {
		if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
			stopMeasureContinuous();
		}

		if (mSocket != INVALID_SOCKET) {
			close(mSocket);
			mSocket = INVALID_SOCKET;
		}

		if (mFrameBuffer != NULL) {
			delete[] mFrameBuffer;
			mFrameBuffer = NULL;
		}
	}

	ResultCode Sensor::reset() {
		if (mSocket == INVALID_SOCKET) {
			return RC_NOT_CONNECTED;
		}
		if (mMeasureThreadHandle != THREAD_INIT_HANDLE) {
			return RC_IN_PROGRESS;
		}
		uint8 command[2] = { 0x63, 0x00 };
		ResultCode rc = sendCommand(command, 2);
		if (rc != RC_OK) {
			return rc;
		}
		rc = receiveFrame(mFrameBuffer, 0, NULL);
		if (rc != RC_OK) {
			return rc;
		}
		
		return RC_OK;
	}

	#if 0
	static void* Sensor::MeasureThreadProc(void *arg) {
		Sensor *ths = (Sensor *)(arg);

		ths->measureContinuous();
	}
	#endif

	void Sensor::measureContinuous() {
		ResultCode rc;
		int extraLength = 0;

		
		while(mMeasureContinuous) {
			rc = receiveFrame(mFrameBuffer, extraLength, &extraLength);
			if (rc != RC_OK) {
				mMeasureCallback->onError(this, rc);
			} else {
				int dataLength = mFrameBuffer[2] | (mFrameBuffer[3] << 8);
				if (dataLength != 643) {
					mMeasureCallback->onError(this, RC_INVALID_RESPONSE);
				} else {
					mMeasureCallback->onDistance(this, (uint16 *)(mFrameBuffer + 6), 320);
				}
			}
		}

		mMeasureThreadHandle = THREAD_INIT_HANDLE;
		pthread_exit(0);
	}

	ResultCode Sensor::sendCommand(const uint8 *data, int size) {
		if (size > MAX_DATA_LENGTH) {
			return RC_INVALID_ARGUMENT;
		}
		int res;

		mFrameBuffer[0] = 0xAA;
		mFrameBuffer[1] = 0x55;
		mFrameBuffer[2] = (size + 2) & 0xFF;
		mFrameBuffer[3] = ((size + 2) >> 8) & 0xFF;
		mFrameBuffer[4] = 0xFF; // address
		memcpy(mFrameBuffer + 5, data, size);
		uint8 sum = 0;
		for (int index = 0; index < 5 + size; index++) {
			sum += mFrameBuffer[index];
		}
		mFrameBuffer[5 + size] = sum;

		int frameLength = 5 + size + 1;
		
		res = sendto(mSocket, (const char *)mFrameBuffer, frameLength, 0, 
			(const struct sockaddr *)&mSensorAddress, sizeof(mSensorAddress));
		if (res != frameLength) {
			return RC_COMMUNICATION_FAILED;
		}

		return RC_OK;
	}

	ResultCode Sensor::receiveFrame(uint8 *frame, int offset, int *extraLength) {
		for (;;) {
			if (offset >= 5) {
				if (frame[0] == 0xAA && frame[1] == 0x55) {
					int dataLength = frame[2] | (frame[3] << 8);
					// fixed
					if (dataLength == 0x8002) {
						dataLength = 643;						
					}
					if (dataLength > MAX_DATA_LENGTH) {
						offset = syncFrame(frame, offset, 2);
					}
					if (offset >= dataLength + 4) {
						uint8 sum = 0;
						for (int index = 0; index < dataLength + 3; index++) {
							sum += frame[index];
						}
						// fixed
						if (dataLength == 643) {
							frame[2] = (uint8)dataLength;
							frame[3] = (uint8)(dataLength >> 8);
						}
						if (frame[dataLength + 3] == sum) {
							if (extraLength != NULL) {
								*extraLength = offset - (dataLength + 4);
							}

							break;
						} else {
							offset = syncFrame(frame, offset, 2);
						}
					}
				} else {

					offset = syncFrame(frame, offset, 1);
				}
			}
			sockaddr_in remoteAddress;
			int remoteAddressLength = sizeof(remoteAddress);
			int rc = recvfrom(mSocket, (char *)frame + offset, MAX_FRAME_LENGTH - offset, 0, 
				(struct sockaddr *)&remoteAddress, (socklen_t *)&remoteAddressLength);
			if (rc <= 0) {
				if (extraLength != NULL) {
					*extraLength = offset;
				}
				return rc == 0 ? RC_CONNECTION_CLOSED : RC_COMMUNICATION_FAILED;
			}
			if (remoteAddress.sin_port == mSensorAddress.sin_port && 
				remoteAddress.sin_addr.s_addr == mSensorAddress.sin_addr.s_addr) {
				offset += rc;
			}
			else {
				
			}	
		}

		return RC_OK;
	}

	int Sensor::syncFrame(uint8 *frame, int length, int start) {
		while (start < length - 1) {
			if (frame[start] == 0xAA && frame[start + 1] == 0x55) {
				break;
			}
			start++;
		}
		if (start > 0) {
			memmove(frame, frame + start, length - start);
		}
		return length - start;
	}

} // namespace lt602

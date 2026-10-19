-- KEYS[1] = vel:acc:{accountId}
-- ARGV[1] = now (epoch ms)
-- ARGV[2] = window start (epoch ms)
-- ARGV[3] = member (txnId)
-- ARGV[4] = ttl (ms)
local key         = KEYS[1]
local now         = tonumber(ARGV[1])
local windowStart = tonumber(ARGV[2])

redis.call('ZREMRANGEBYSCORE', key, '-inf', '(' .. windowStart)
redis.call('ZADD', key, now, ARGV[3])
redis.call('PEXPIRE', key, tonumber(ARGV[4]))
return redis.call('ZCOUNT', key, windowStart, now)
export interface AuditRecord {
  id: number;
  action: string;
  entityType: string;
  entityId: number;
  performedBy: string;
  details: string;
  timestamp: string;
}
